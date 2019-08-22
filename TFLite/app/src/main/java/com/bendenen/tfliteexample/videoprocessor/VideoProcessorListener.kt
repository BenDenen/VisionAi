package com.bendenen.tfliteexample.videoprocessor

import android.graphics.Bitmap

interface VideoProcessorListener {

    fun onNewFrameProcessed(bitmap: Bitmap)

}