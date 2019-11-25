package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

interface StepResult<T> {

    fun getSourceBitmap(): Bitmap

    fun getResult(): T

    fun getBitmapForNextStep() : Bitmap

}