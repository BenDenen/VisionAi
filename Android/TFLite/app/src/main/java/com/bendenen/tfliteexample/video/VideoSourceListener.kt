package com.bendenen.tfliteexample.video

import android.graphics.Bitmap

interface VideoSourceListener {

    fun onNewFrame(rgbBytes: ByteArray)

    fun onNewBitmap(bitmap: Bitmap)

}