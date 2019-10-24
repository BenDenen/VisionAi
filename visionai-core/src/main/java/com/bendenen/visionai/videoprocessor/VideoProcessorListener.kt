package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

interface VideoProcessorListener {

    fun onNewFrameProcessed(bitmap: Bitmap)
}