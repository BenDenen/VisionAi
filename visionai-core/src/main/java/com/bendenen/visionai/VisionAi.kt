package com.bendenen.visionai

import android.graphics.Bitmap
import android.util.Log
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener

object VisionAi : VideoProcessorListener {

    interface ResultListener {
        fun onFrameResult(bitmap: Bitmap)
        fun onFileResult(filePath: String)
    }

    val TAG = VisionAi::class.java.simpleName

    private lateinit var videoProcessor: VideoProcessor
    private var resultListener: ResultListener? = null

    fun init(
        videoProcessor: VideoProcessor
    ) {
        this.videoProcessor = videoProcessor.also {
            it.setListener(this)
        }
    }

    fun start(resultListener: ResultListener) {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        this.resultListener = resultListener
        videoProcessor.start()
    }

    fun stop() {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        videoProcessor.stop()
    }

    fun release() {
        // TODO: Release resources
    }

    override fun onNewFrameProcessed(bitmap: Bitmap) {
        this.resultListener?.onFrameResult(bitmap)
    }
}