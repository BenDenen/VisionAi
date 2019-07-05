package com.bendenen.tfliteexample.videosource.mediacodec

import android.animation.TimeAnimator
import android.app.Application
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.RenderScript
import android.util.Log
import android.util.Size
import com.bendenen.tfliteexample.videosource.VideoSource
import com.bendenen.tfliteexample.videosource.VideoSourceListener
import com.bendenen.tfliteexample.videosource.render.ImageRender
import com.bendenen.tfliteexample.videosource.render.RenderActionsListener
import com.bendenen.tfliteexample.videosource.render.rs.IntrinsicRenderScriptImageRender

class MediaCodecVideoSourceImpl(
    application: Application,
    private val requestedWidth: Int,
    private val requestedHeight: Int,
    val uri: Uri
) : VideoSource, RenderActionsListener, OutputBufferListener {

    companion object {
        const val TAG = "MediaCodecSource"
    }

    private lateinit var imageRender: ImageRender

    private var videoSourceListener: VideoSourceListener? = null

    private lateinit var codecWrapper: MediaCodecHandler
    private val extractor = MediaExtractor()

    private val timeAnimator = TimeAnimator()

    private var useBitmap = false

    private var videoWidth = 0
    private var videoHeight = 0

    private var isFrameRendering = false

    private val handlerThread = HandlerThread("HandlerThread").also { it.start() }
    private val executorHandler = Handler(handlerThread.looper)

    init {

        extractor.setDataSource(
            application, uri, null
        )

        val nTracks = extractor.trackCount

        // Begin by unselecting all of the tracks in the extractor, so we won't see
        // any tracks that we haven't explicitly selected.
        for (i in 0 until nTracks) {
            extractor.unselectTrack(i)
        }


        // Find the first video track in the stream. In a real-world application
        // it's possible that the stream would contain multiple tracks, but this
        // sample assumes that we just want to play the first one.
        for (i in 0 until nTracks) {
            // Try to create a video codec for this track. This call will return null if the
            // track is not a video track, or not a recognized video format. Once it returns
            // a valid MediaCodecWrapper, we can break out of the loop.

            val trackFormat = extractor.getTrackFormat(i)

            val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)

            if (mimeType.contains("video/")) {

                if (!::imageRender.isInitialized) {
                    videoWidth = trackFormat.getInteger(MediaFormat.KEY_WIDTH)
                    videoHeight = trackFormat.getInteger(MediaFormat.KEY_HEIGHT)

                    Log.d(TAG, " videoWidth: $videoWidth  videoHeight: $videoHeight")
                    Log.d(TAG, " trackFormat: $trackFormat")

                    imageRender = IntrinsicRenderScriptImageRender(
                        RenderScript.create(application),
                        Size(videoWidth, videoHeight)
                    )
                    imageRender.renderActionsListener = this
                }

                codecWrapper = MediaCodecHandler(
                    trackFormat,
                    imageRender.getSurface(),
                    this
                )
                extractor.selectTrack(i)
                break
            }

        }

        if (!::codecWrapper.isInitialized) {
            throw IllegalArgumentException()
        }
    }


    // Video Source Actions
    override fun getSourceWidth(): Int = videoWidth

    override fun getSourceHeight(): Int = videoHeight

    override fun isAttached(): Boolean = videoSourceListener != null

    override fun attach(videoSourceListener: VideoSourceListener) {

        this.videoSourceListener = videoSourceListener

        executorHandler.post { startDataRetrieving() }


        // By using a {@link TimeAnimator}, we can sync our media rendering commands with
        // the system display frame rendering. The animator ticks as the {@link Choreographer}
        // receives VSYNC events.
//        var counter = 0
//        timeAnimator.setTimeListener { animation, totalTime, deltaTime ->
//
//            val isEos = extractor.sampleFlags and MediaCodec
//                .BUFFER_FLAG_END_OF_STREAM == MediaCodec.BUFFER_FLAG_END_OF_STREAM
//
//            // BEGIN_INCLUDE(write_sample)
//            if (!isEos) {
//                // Try to submit the sample to the codec and if successful advance the
//                // extractor to the next available sample to read.
//                val result = codecWrapper.writeSample(
//                    extractor, false,
//                    extractor.sampleTime, extractor.sampleFlags
//                ) ?: false
//
//                if (result) {
//                    // Advancing the extractor is a blocking operation and it MUST be
//                    // executed outside the main thread in real applications.
//                    Log.e("MyTag", "advance " + counter++)
//                    extractor.advance()
//                }
//            }
//            // END_INCLUDE(write_sample)
//
//            // Examine the sample at the head of the queue to see if its ready to be
//            // rendered and is not zero sized End-of-Stream record.
//            val out_bufferInfo = MediaCodec.BufferInfo()
//            codecWrapper.peekSample(out_bufferInfo)
//
//            // BEGIN_INCLUDE(render_sample)
//            if (out_bufferInfo.size <= 0 && isEos) {
//                timeAnimator.end()
//                codecWrapper.stopAndRelease()
//                extractor.release()
////                this.videoSourceListener?.onFinish()
//            } else if (out_bufferInfo.presentationTimeUs / 1000 < totalTime)  {
//                // Pop the sample off the queue and send it to {@link Surface}
//
//                codecWrapper.popSample(true)
//            }
//            // END_INCLUDE(render_sample)
//        }
//
////        // We're all set. Kick off the animator to process buffers and render video frames as
////        // they become available
////        this.videoSourceListener = videoSourceListener
//        timeAnimator.start()
    }

    override fun useBitmap(useBitmap: Boolean) {
        this.useBitmap = useBitmap
    }

    override fun detach() {
        if (timeAnimator.isRunning) {
            timeAnimator.end()
            codecWrapper.stopAndRelease()
            extractor.release()
            videoSourceListener = null
        }

    }

    override fun release() {
        executorHandler.removeCallbacksAndMessages(null)
        imageRender.release()
    }


    // Render actions
    override fun onNewRGBBytes(byteArray: ByteArray) {
        // TODO: Change to use flag
        videoSourceListener?.onNewFrame(byteArray)
    }

    override fun onNewBitmap(bitmap: Bitmap) {
        videoSourceListener?.onNewBitmap(bitmap)
        isFrameRendering = false
    }

    override fun useBitmap(): Boolean = useBitmap
    // end

    override fun onIsNotEmpty() {
        isFrameRendering = true
    }

    private fun startDataRetrieving() {
        val out_bufferInfo = MediaCodec.BufferInfo()
        var counter = 0

        while (extractor.sampleFlags and MediaCodec
                .BUFFER_FLAG_END_OF_STREAM != MediaCodec.BUFFER_FLAG_END_OF_STREAM
        ) {
            if(isFrameRendering) {
                continue
            }



            // Try to submit the sample to the codec and if successful advance the
            // extractor to the next available sample to read.
            val result = codecWrapper.writeSample(
                extractor, false,
                extractor.sampleTime, extractor.sampleFlags
            )

            if (result) {
                // Advancing the extractor is a blocking operation and it MUST be
                // executed outside the main thread in real applications.
                val isDone = extractor.advance()
                Log.e("MyTag", "advance $isDone" + counter++)

                codecWrapper.popSample(true)
            }

            codecWrapper.peekSample(out_bufferInfo)
            // END_INCLUDE(render_sample)
        }
        codecWrapper.stopAndRelease()
        extractor.release()
        this.videoSourceListener?.onFinish()


    }


}