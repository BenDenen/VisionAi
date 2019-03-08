package com.bendenen.tfliteexample.video.camera2

import android.graphics.Bitmap


interface RenderActionsListener {

    fun getRgbBytesArray(): ByteArray

    fun getBitmap(): Bitmap

    fun onDataReady()
}