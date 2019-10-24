package com.bendenen.visionai.videoprocessor

abstract class VideoProcessor {

    internal abstract fun setListener(videoProcessorListener: VideoProcessorListener)

    abstract fun start()

    abstract fun stop()
}