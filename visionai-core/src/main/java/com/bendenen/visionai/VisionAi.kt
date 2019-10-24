package com.bendenen.visionai

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener
import com.bendenen.visionai.videoprocessor.outputencoder.OutputEncoder
import com.bendenen.visionai.videoprocessor.outputencoder.mediamuxer.MediaMuxerOutputEncoderImpl
import com.bendenen.visionai.videosource.VideoSource
import com.bendenen.visionai.videosource.mediacodec.MediaCodecVideoSourceImpl
import java.io.File

object VisionAi : VideoProcessorListener {

    interface ResultListener {
        fun onFrameResult(bitmap: Bitmap)
        fun onFileResult(filePath: String)
    }

    val TAG = VisionAi::class.java.simpleName

    private lateinit var videoProcessor: VideoProcessor

    private lateinit var videoSource: VideoSource
    private lateinit var outputEncoder: OutputEncoder

    private var resultListener: ResultListener? = null

    fun init(
        application: Application,
        uri: Uri,
        videoProcessor: VideoProcessor
    ) {
        init(
            MediaCodecVideoSourceImpl(
                application,
                uri
            ),
            videoProcessor = videoProcessor
        )
    }

    fun init(
        videoSource: VideoSource,
        outputEncoder: OutputEncoder = MediaMuxerOutputEncoderImpl(),
        videoProcessor: VideoProcessor
    ) {
        this.videoSource = videoSource.also {
            it.useBitmap(true)
        }
        this.videoProcessor = videoProcessor.also {
            it.init(
                this.videoSource.getSourceWidth(),
                this.videoSource.getSourceHeight()
            )
            it.setListener(this)
        }

        this.outputEncoder = outputEncoder.also {
            it.initialize(
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "temp.mp4"
                ),
                this.videoSource.getSourceWidth(),
                this.videoSource.getSourceHeight()
            )
        }
    }

    fun start(resultListener: ResultListener) {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        this.resultListener = resultListener
        videoSource.attach(videoProcessor)
    }

    fun stop() {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        videoSource.detach()
    }

    fun release() {
        // TODO: Release resources
    }

    override fun onNewFrameProcessed(bitmap: Bitmap) {
        outputEncoder.encodeBitmap(Bitmap.createBitmap(bitmap))
        resultListener?.onFrameResult(bitmap)
    }

    override fun onFinish() {
        outputEncoder.finish()
    }
}