package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

interface ProcessorStep {

    fun getWidthForNextStep(): Int

    fun getHeightForNextStep(): Int

    fun init(
        videoSourceWidth: Int,
        videoSourceHeight: Int
    )

    fun applyForData(
        bitmap: Bitmap
    ): Bitmap

}