package com.bendenen.visionai

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener
import com.bendenen.visionai.outputencoder.OutputEncoder
import com.bendenen.visionai.outputencoder.mediamuxer.MediaMuxerOutputEncoderImpl
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
        visionAiConfig: VisionAiConfig,
        ready: () -> Unit
    ) {

        fun initOutputEncoder() {
            outputEncoder.initialize(
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "temp.mp4"
                ),
                this.videoSource.getSourceWidth(),
                this.videoSource.getSourceHeight()
            )
        }

        outputEncoder = visionAiConfig.outputEncoder ?: MediaMuxerOutputEncoderImpl()
        if (visionAiConfig.videoSource != null) {
            videoSource = visionAiConfig.videoSource
            initOutputEncoder()
            ready.invoke()
        } else if (visionAiConfig.videoUri != null && visionAiConfig.application != null) {
            videoSource = MediaCodecVideoSourceImpl(visionAiConfig.application).also {
                it.loadVideoFile(
                    visionAiConfig.videoUri
                ) {
                    initOutputEncoder()
                    ready.invoke()
                }
            }
        } else {
            throw IllegalArgumentException(" Not enough information about video source ")
        }
        videoSource.useBitmap(true)
    }

    fun setProcessor(
        videoProcessor: VideoProcessor
    ) {
        this.videoProcessor = videoProcessor.also {
            it.init(
                videoSource.getSourceWidth(),
                videoSource.getSourceHeight()
            )
            it.setListener(this)
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