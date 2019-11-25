package com.bendenen.visionai.videosource

import android.graphics.Bitmap

interface VideoSourceListener {

    fun onNewFrame(rgbBytes: ByteArray)

    fun onNewBitmap(bitmap: Bitmap)

    fun onFinish()
}