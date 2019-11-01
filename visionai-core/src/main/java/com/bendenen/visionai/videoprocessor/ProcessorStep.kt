package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

interface ProcessorStep {

    fun getWidthForNextStep(): Int

    fun getHeightForNextStep(): Int

    suspend fun init(
        videoSourceWidth: Int,
        videoSourceHeight: Int
    )

     fun applyForData(
        bitmap: Bitmap
    ): Bitmap

     fun applyForData(
        rgbBytes: ByteArray,
        width: Int,
        height: Int
    ): Bitmap
}