package com.bendenen.visionai.videoprocessor

interface VideoProcessor {

    var videoProcessorListener: VideoProcessorListener?

    fun start()

    fun stop()
}