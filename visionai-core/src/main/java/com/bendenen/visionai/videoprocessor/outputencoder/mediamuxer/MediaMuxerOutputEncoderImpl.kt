package com.bendenen.visionai.videoprocessor.outputencoder.mediamuxer

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodec.CONFIGURE_FLAG_ENCODE
import android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
import android.media.MediaCodec.INFO_TRY_AGAIN_LATER
import android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
import android.media.MediaFormat
import android.media.MediaFormat.KEY_BIT_RATE
import android.media.MediaFormat.KEY_COLOR_FORMAT
import android.media.MediaFormat.KEY_FRAME_RATE
import android.media.MediaFormat.KEY_I_FRAME_INTERVAL
import android.media.MediaFormat.MIMETYPE_VIDEO_AVC
import android.media.MediaMuxer
import android.util.Log
import androidx.annotation.WorkerThread
import com.bendenen.visionai.videoprocessor.outputencoder.OutputEncoder
import java.io.File

class MediaMuxerOutputEncoderImpl(
    val outputVideoWidth: Int,
    val outputVideoHeight: Int
) : OutputEncoder {

    private var currentState = OutputEncoder.EncoderState.NOT_INITIALIZED

    private val bufferInfo = MediaCodec.BufferInfo()
    private var videoCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC)
    private lateinit var mediaMuxer: MediaMuxer

    private var trackCount = 0
    private var videoTrackIndex: Int = 0
    private var isMuxerStarted = false
    private var encodedFrameCount: Int = 0
    private var addedFrameCount: Int = 0

    override fun initialize(outputFile: File) {
        val videoFormat =
            MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, outputVideoWidth, outputVideoHeight)
        videoFormat.setInteger(KEY_BIT_RATE, BIT_RATE)
        videoFormat.setInteger(KEY_FRAME_RATE, FRAME_RATE)
        videoFormat.setInteger(KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        videoFormat.setInteger(KEY_COLOR_FORMAT, COLOR_FormatYUV420Flexible)
        videoCodec.configure(videoFormat, null, null, CONFIGURE_FLAG_ENCODE)
        videoCodec.start()

        mediaMuxer =
            MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        currentState = OutputEncoder.EncoderState.INITIALIZED
    }

    override fun getEncoderState(): OutputEncoder.EncoderState = currentState

    @WorkerThread
    override fun encodeBitmap(bitmap: Bitmap) {
        if (currentState == OutputEncoder.EncoderState.FINISHED) {
            Log.d(TAG, "already finished. can't add Frame ")
        } else {
            val inputBufIndex = videoCodec.dequeueInputBuffer(TIMEOUT_US.toLong())
            if (inputBufIndex >= 0) {
                val input = getNV12(bitmap.width, bitmap.height, bitmap)
                val inputBuffer = videoCodec.getInputBuffer(inputBufIndex)
                inputBuffer!!.clear()
                inputBuffer.put(input)
                videoCodec.queueInputBuffer(
                    inputBufIndex, 0, input.size,
                    getPresentationTimeUsec(addedFrameCount), 0
                )
            }
            addedFrameCount++
            while (addedFrameCount > encodedFrameCount) {
                encode()
            }
        }
    }

    override fun finish() {

        if (currentState != OutputEncoder.EncoderState.FINISHED) {
            encode()
            if (this.addedFrameCount > 0) {
                Log.i(TAG, "Total frame count = " + this.addedFrameCount)
                if (videoCodec != null) {
                    videoCodec.stop()
                    videoCodec.release()
//                    videoCodec = null
                    Log.i(TAG, "RELEASE VIDEO CODEC")
                }
                if (mediaMuxer != null) {
                    mediaMuxer.stop()
                    mediaMuxer.release()
                    Log.i(TAG, "RELEASE MUXER")
                }
            } else {
                Log.e(TAG, "not added any frame")
            }
            currentState = OutputEncoder.EncoderState.FINISHED
        }
    }

    private fun encode() {
        val encoderStatus = videoCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US.toLong())

        if (encoderStatus == INFO_OUTPUT_FORMAT_CHANGED) {
            val videoFormat = videoCodec.outputFormat
            videoTrackIndex = mediaMuxer.addTrack(videoFormat)
            mediaMuxer.start()
            isMuxerStarted = true
        } else if (encoderStatus == INFO_TRY_AGAIN_LATER) {
            Log.d(TAG, "no output from video encoder available")
        } else {
            val encodedData = videoCodec.getOutputBuffer(encoderStatus)
            if (encodedData != null) {
                encodedData.position(bufferInfo.offset)
                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                if (isMuxerStarted) {
                    mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                }
                videoCodec.releaseOutputBuffer(encoderStatus, false)
                encodedFrameCount++
            }
            Log.i(TAG, "encoderOutputBuffer $encoderStatus was null")
        }
    }

    private fun getNV12(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
        scaled.recycle()
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        var yIndex = 0
        var uvIndex = width * height
        var index = 0
        var j = 0
        while (j < height) {
            var uvIndex2: Int
            var yIndex2: Int
            var i = 0
            while (true) {
                uvIndex2 = uvIndex
                yIndex2 = yIndex
                if (i >= width) {
                    break
                }
                val R = argb[index] and 0xFF0000 shr 16
                val G = argb[index] and 0xFF00 shr 8
                val B = argb[index] and 0x0000FF shr 0
                var Y = R * 77 + G * 150 + B * 29 + 128 shr 8
                var V = (R * -43 - G * 84 + B * 127 + 128 shr 8) + 128
                var U = (R * 127 - G * 106 - B * 21 + 128 shr 8) + 128
                yIndex = yIndex2 + 1
                if (Y < 0) {
                    Y = 0
                } else if (Y > 255) {
                    Y = 255
                }
                yuv420sp[yIndex2] = Y.toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    uvIndex = uvIndex2 + 1
                    if (V < 0) {
                        V = 0
                    } else if (V > 255) {
                        V = 255
                    }
                    yuv420sp[uvIndex2] = V.toByte()
                    uvIndex2 = uvIndex + 1
                    if (U < 0) {
                        U = 0
                    } else if (U > 255) {
                        U = 255
                    }
                    yuv420sp[uvIndex] = U.toByte()
                }
                uvIndex = uvIndex2
                index++
                i++
            }
            j++
            uvIndex = uvIndex2
            yIndex = yIndex2
        }
    }

    private fun getPresentationTimeUsec(frameIndex: Int): Long {
        return frameIndex.toLong() * ONE_SEC / 20
    }

    companion object {
        private const val TAG = "OutputEncoder"
        private const val BIT_RATE = 2000000
        private const val FRAME_RATE = 20
        private const val I_FRAME_INTERVAL = 5
        private const val TIMEOUT_US = 10000

        private const val ONE_SEC = 1000000L
    }
}