package com.bendenen.visionai.tflite.styletransfer

import android.graphics.Bitmap

interface ArtisticStyleTransfer {

    fun setStyle(styleBitmap: Bitmap)

    fun styleTransform(
        contentImageData: ByteArray,
        imageWidth: Int,
        imageHeight: Int
    ): Array<Array<Array<FloatArray>>>

    fun getStyleImageSize(): Int

    fun getContentImageSize(): Int

    fun close()
}