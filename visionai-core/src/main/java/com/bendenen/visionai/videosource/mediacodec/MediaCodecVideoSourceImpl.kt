package com.bendenen.visionai.videosource.mediacodec

import android.animation.TimeAnimator
import android.app.Application
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.renderscript.RenderScript
import android.util.Log
import android.util.Size
import com.bendenen.visionai.videosource.MediaFileVideoSource
import com.bendenen.visionai.videosource.VideoSourceListener
import com.bendenen.visionai.videosource.render.ImageRender
import com.bendenen.visionai.videosource.render.RenderActionsListener
import com.bendenen.visionai.videosource.render.rs.IntrinsicRenderScriptImageRender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MediaCodecVideoSourceImpl(
    private val application: Application
) : MediaFileVideoSource, RenderActionsListener, OutputBufferListener, CoroutineScope {

    companion object {
        const val TAG = "MediaCodecSource"
    }

    private lateinit var imageRender: ImageRender

    private var videoSourceListener: VideoSourceListener? = null

    private lateinit var codecWrapper: MediaCodecHandler
    private val extractor = MediaExtractor()
    private lateinit var videoUri: Uri

    private val timeAnimator = TimeAnimator()

    private var useBitmap = false

    private var videoWidth = 0
    private var videoHeight = 0

    private var isFrameRendering = false

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // Video Source Actions
    override fun getSourceWidth(): Int = videoWidth

    override fun getSourceHeight(): Int = videoHeight

    override fun isAttached(): Boolean = videoSourceListener != null

    override fun attach(videoSourceListener: VideoSourceListener) {

        this.videoSourceListener = videoSourceListener

        launch {
            startDataRetrieving()
        }
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
        job.cancel()
        imageRender.release()
    }

    // MediaFileVideoSource

    suspend override fun loadVideoFile(uri: Uri) =
        withContext(Dispatchers.IO) {

            extractor.setDataSource(
                application, uri, null
            )
            videoUri = uri

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
                        imageRender.renderActionsListener = this@MediaCodecVideoSourceImpl
                    }

                    codecWrapper = MediaCodecHandler(
                        trackFormat,
                        imageRender.getSurface(),
                        this@MediaCodecVideoSourceImpl
                    )
                    extractor.selectTrack(i)
                    break
                }
            }

            if (!::codecWrapper.isInitialized) {
                throw IllegalArgumentException()
            }
        }

    override suspend fun requestPreview(
        timestamp: Long
    ) = getPreviewForTime(timestamp)

    // Render actions

    // For debug
    override fun onNewData(rgbBytes: ByteArray, bitmap: Bitmap) {
        videoSourceListener?.onNewData(rgbBytes, bitmap)
        isFrameRendering = false
    }

    override fun onNewRGBBytes(byteArray: ByteArray) {
        // TODO: Change to use flag
        videoSourceListener?.onNewFrame(byteArray)
    }

    override fun onNewBitmap(bitmap: Bitmap) {
//        videoSourceListener?.onNewBitmap(bitmap)
//        isFrameRendering = false
    }

    override fun useBitmap(): Boolean = useBitmap
    // end

    override fun onIsNotEmpty() {
        isFrameRendering = true
    }

    private suspend fun getPreviewForTime(timestamp: Long): Bitmap =
        withContext(Dispatchers.IO) {
            val mediaMetadataRetriever = MediaMetadataRetriever()

            mediaMetadataRetriever.setDataSource(application, videoUri)
            return@withContext mediaMetadataRetriever.getFrameAtTime(timestamp)
        }

    private suspend fun startDataRetrieving() =
        withContext(Dispatchers.IO) {
            val outBuffering = MediaCodec.BufferInfo()
            var counter = 0

            while (extractor.sampleFlags and MediaCodec
                    .BUFFER_FLAG_END_OF_STREAM != MediaCodec.BUFFER_FLAG_END_OF_STREAM
            ) {
                if (isFrameRendering) {
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

                    codecWrapper.popSample(true)
                }

                codecWrapper.peekSample(outBuffering)
                // END_INCLUDE(render_sample)
            }
            codecWrapper.stopAndRelease()
            extractor.release()
            videoSourceListener?.onFinish()
        }
}