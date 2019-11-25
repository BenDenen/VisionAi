package com.bendenen.visionai.videosource.render

import android.graphics.Bitmap

interface RenderActionsListener {

    fun onNewBitmap(bitmap: Bitmap)

    fun onNewRGBBytes(byteArray: ByteArray)

    fun useBitmap(): Boolean
}