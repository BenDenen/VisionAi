package com.bendenen.visionai.videoprocessor

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

}