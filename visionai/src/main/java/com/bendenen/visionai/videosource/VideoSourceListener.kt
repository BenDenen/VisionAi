package com.bendenen.visionai.videosource

import android.graphics.Bitmap

interface VideoSourceListener {

    fun onNewFrame(rgbBytes: ByteArray)

    fun onNewBitmap(bitmap: Bitmap)

    // For debug
    fun onNewData(rgbBytes: ByteArray, bitmap: Bitmap){}

    fun onFinish()
}