package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap
import com.bendenen.visionai.videosource.VideoSourceListener

abstract class VideoProcessor : VideoSourceListener {

    protected var videoProcessorListener: VideoProcessorListener? = null

    internal fun setListener(videoProcessorListener: VideoProcessorListener) {
        this.videoProcessorListener = videoProcessorListener
    }

    abstract fun init(
        videoSourceWidth: Int,
        videoSourceHeight: Int
    )

    abstract suspend fun applyForData(
        bitmap: Bitmap
    ): Bitmap

    abstract suspend fun applyForData(
        rgbBytes: ByteArray,
        width: Int,
        height: Int
    ): Bitmap
}