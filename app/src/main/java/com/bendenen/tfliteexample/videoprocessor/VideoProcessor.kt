package com.bendenen.tfliteexample.videoprocessor

interface VideoProcessor {

    var videoProcessorListener:VideoProcessorListener?

    fun start()

    fun stop()

}