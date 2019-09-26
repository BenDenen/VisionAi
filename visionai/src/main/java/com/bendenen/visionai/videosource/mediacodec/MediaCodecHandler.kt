package com.bendenen.visionai.videosource.mediacodec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.util.ArrayDeque
import java.util.Locale
import java.util.Queue

class MediaCodecHandler(
    trackFormat: MediaFormat,
    surface: Surface,
    val outputBufferListener: OutputBufferListener? = null
) {

    companion object {
        private const val TAG = "MediaCodecHandler"
        private val CRYPTO_INFO = MediaCodec.CryptoInfo()
    }

    private val callBackHandler = Handler()
    private val decoder: MediaCodec

    // References to the internal buffers managed by the codec. The codec
    // refers to these buffers by index, never by reference so it's up to us
    // to keep track of which buffer is which.
    private val inputBuffers: Array<ByteBuffer>
    private var outputBuffers: Array<ByteBuffer>

    // Indices of the input buffers that are currently available for writing. We'll
    // consume these in the order they were dequeued from the codec.
    private val availableInputBuffers: Queue<Int>

    // Indices of the output buffers that currently hold valid data, in the order
    // they were produced by the codec.
    private val availableOutputBuffers: Queue<Int>

    // Information about each output buffer, by index. Each entry in this array
    // is valid if and only if its index is currently contained in availableOutputBuffers.
    private var outputBufferInfo: Array<MediaCodec.BufferInfo?>

    init {
        // Decoder initializing
        val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)

        if (!mimeType.contains("video/")) {
            throw IllegalArgumentException("MediaFormat should contain video information")
        }

        Log.d(TAG, "mimeType $mimeType")

        decoder = MediaCodec.createDecoderByType(mimeType)
        // Change output color format to COLOR_FormatYUV420Flexible
        trackFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        decoder.configure(trackFormat, surface, null, 0)

        Log.d(TAG, "COLOR_FORMAT " + decoder.outputFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT))
        decoder.start()

        // Buffers initializing
        inputBuffers = decoder.inputBuffers
        outputBuffers = decoder.outputBuffers
        outputBufferInfo = arrayOfNulls(outputBuffers.size)
        availableInputBuffers = ArrayDeque<Int>(outputBuffers.size)
        availableOutputBuffers = ArrayDeque<Int>(inputBuffers.size)
    }

    /**
     * Write a media sample to the decoder.
     *
     * A "sample" here refers to a single atomic access unit in the media stream. The definition
     * of "access unit" is dependent on the type of encoding used, but it typically refers to
     * a single frame of video or a few seconds of audio. [MediaExtractor]
     * extracts data from a stream one sample at a time.
     *
     * @param input A ByteBuffer containing the input data for one sample. The buffer must be set
     * up for reading, with its position set to the beginning of the sample data and its limit
     * set to the end of the sample data.
     *
     * @param presentationTimeUs The time, relative to the beginning of the media stream,
     * at which this buffer should be rendered.
     *
     * @param flags Flags to pass to the decoder. See [MediaCodec.queueInputBuffer]
     *
     * @throws MediaCodec.CryptoException
     */
    @Throws(MediaCodec.CryptoException::class, IllegalArgumentException::class)
    fun writeSample(
        input: ByteBuffer,
        crypto: MediaCodec.CryptoInfo?,
        presentationTimeUs: Long,
        flags: Int
    ): Boolean {
        var result = false
        val size = input.remaining()

        // check if we have dequed input buffers available from the codec
        if (size > 0 && !availableInputBuffers.isEmpty()) {
            val index = availableInputBuffers.remove()
            val buffer = inputBuffers[index]

            // we can't write our sample to a lesser capacity input buffer.
            if (size > buffer.capacity()) {
                throw IllegalArgumentException(
                    String.format(
                        Locale.US,
                        "Insufficient capacity in MediaCodec buffer: " + "tried to write %d, buffer capacity is %d.",
                        input.remaining(),
                        buffer.capacity()
                    )
                )
            }

            buffer.clear()
            buffer.put(input)

            // Submit the buffer to the codec for decoding. The presentationTimeUs
            // indicates the position (play time) for the current sample.
            if (crypto == null) {
                decoder.queueInputBuffer(index, 0, size, presentationTimeUs, flags)
            } else {
                decoder.queueSecureInputBuffer(index, 0, crypto, presentationTimeUs, flags)
            }
            result = true
        }
        return result
    }

    /**
     * Write a media sample to the decoder.
     *
     * A "sample" here refers to a single atomic access unit in the media stream. The definition
     * of "access unit" is dependent on the type of encoding used, but it typically refers to
     * a single frame of video or a few seconds of audio. [MediaExtractor]
     * extracts data from a stream one sample at a time.
     *
     * @param extractor Instance of [MediaExtractor] wrapping the media.
     *
     * @param presentationTimeUs The time, relative to the beginning of the media stream,
     * at which this buffer should be rendered.
     *
     * @param flags Flags to pass to the decoder. See [MediaCodec.queueInputBuffer]
     *
     * @throws MediaCodec.CryptoException
     */
    fun writeSample(
        extractor: MediaExtractor,
        isSecure: Boolean,
        presentationTimeUs: Long,
        flags: Int
    ): Boolean {
        var internalFlags = flags
        var result = false

        if (!availableInputBuffers.isEmpty()) {
            val index = availableInputBuffers.remove()
            val buffer = inputBuffers[index]

            // reads the sample from the file using extractor into the buffer
            val size = extractor.readSampleData(buffer, 0)
            if (size <= 0) {
                internalFlags = flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
            }

            // Submit the buffer to the codec for decoding. The presentationTimeUs
            // indicates the position (play time) for the current sample.
            if (!isSecure) {
                decoder.queueInputBuffer(index, 0, size, presentationTimeUs, internalFlags)
            } else {
                extractor.getSampleCryptoInfo(CRYPTO_INFO)
                decoder.queueSecureInputBuffer(
                    index,
                    0,
                    CRYPTO_INFO,
                    presentationTimeUs,
                    internalFlags
                )
            }

            result = true
        }
        return result
    }

    /**
     * Performs a peek() operation in the queue to extract media info for the buffer ready to be
     * released i.e. the head element of the queue.
     *
     * @param outBufferInfo An output var to hold the buffer info.
     *
     * @return True, if the peek was successful.
     */
    fun peekSample(outBufferInfo: MediaCodec.BufferInfo): Boolean {
        // dequeue available buffers and synchronize our data structures with the codec.
        update()
        var result = false
        if (!availableOutputBuffers.isEmpty()) {
            val index = availableOutputBuffers.peek()
            outputBufferInfo[index]?.let {
                // metadata of the sample
                outBufferInfo.set(
                    it.offset,
                    it.size,
                    it.presentationTimeUs,
                    it.flags
                )
            }

            result = true
        }
        return result
    }

    /**
     * Processes, releases and optionally renders the output buffer available at the head of the
     * queue. All observers are notified with a callback. See [ ][OutputSampleListener.outputSample]
     *
     * @param render True, if the buffer is to be rendered on the [Surface] configured
     */
    fun popSample(render: Boolean) {
        // dequeue available buffers and synchronize our data structures with the codec.
        update()
        if (!availableOutputBuffers.isEmpty()) {
            outputBufferListener?.onIsNotEmpty()
            val index = availableOutputBuffers.remove()

            // releases the buffer back to the codec
            decoder.releaseOutputBuffer(index, render)
        }
    }

    fun stopAndRelease() {
        decoder.stop()
        decoder.release()
    }

    /**
     * Synchronize this object's state with the internal state of the wrapped
     * MediaCodec.
     */
    private fun update() {
        var indexIn = 0

        // Get valid input buffers from the codec to fill later in the same order they were
        // made available by the codec.
        while ({
                indexIn = decoder.dequeueInputBuffer(0); indexIn
            }() != MediaCodec.INFO_TRY_AGAIN_LATER) {
            availableInputBuffers.add(indexIn)
        }

        // Likewise with output buffers. If the output buffers have changed, start using the
        // new set of output buffers. If the output format has changed, notify listeners.
        val info = MediaCodec.BufferInfo()
        var indexOut = 0
        while ({
                indexOut = decoder.dequeueOutputBuffer(info, 0); indexOut
            }() != MediaCodec.INFO_TRY_AGAIN_LATER) {
            when (indexOut) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    outputBuffers = decoder.getOutputBuffers()
                    outputBufferInfo = arrayOfNulls(outputBuffers.size)
                    availableOutputBuffers.clear()
                }
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
//                    if (mOutputFormatChangedListener != null) {
//                        callBackHandler.post(Runnable {
//                            mOutputFormatChangedListener
//                                .outputFormatChanged(
//                                    this@MediaCodecWrapper,
//                                    mDecoder.getOutputFormat()
//                                )
//                        })
//                    }
                }
                else ->
                    // Making sure the index is valid before adding to output buffers. We've already
                    // handled INFO_TRY_AGAIN_LATER, INFO_OUTPUT_FORMAT_CHANGED &
                    // INFO_OUTPUT_BUFFERS_CHANGED i.e all the other possible return codes but
                    // asserting index value anyways for future-proofing the code.
                    if (indexOut >= 0) {
                        outputBufferInfo[indexOut] = info
                        availableOutputBuffers.add(indexOut)
                    } else {
                        throw IllegalStateException("Unknown status from dequeueOutputBuffer")
                    }
            }
        }
    }
}

interface OutputBufferListener {
    fun onIsNotEmpty()
}