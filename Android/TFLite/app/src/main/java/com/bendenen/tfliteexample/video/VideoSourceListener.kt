package com.bendenen.tfliteexample.video

import android.graphics.Bitmap

internal interface VideoSourceListener {

    fun onNewFrame(rgbBytes: ByteArray)

    fun onNewBitmap(bitmap: Bitmap)

}