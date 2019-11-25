package com.bendenen.visionai.tflite.styletransfer.step

import android.graphics.Bitmap
import com.bendenen.visionai.videoprocessor.StepResult

data class ArtisticStyleTransferStepResult(
    val sourceImage: Bitmap,
    val styledImage: Bitmap,
    val nextStepImage: Bitmap
) : StepResult<Bitmap> {

    override fun getSourceBitmap(): Bitmap = sourceImage

    override fun getResult(): Bitmap = styledImage

    override fun getBitmapForNextStep(): Bitmap = nextStepImage
}