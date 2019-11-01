package com.bendenen.visionai.tflite.videoprocessor.step.styletransfer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.bendenen.visionai.tflite.utils.getTransformationMatrix
import com.bendenen.visionai.tflite.utils.toBitmap
import com.bendenen.visionai.tflite.utils.toByteArray
import com.bendenen.visionai.videoprocessor.ProcessorStep

class ArtisticStyleTransferStep(
    context: Context,
    styleFilePath: String = "test_style.jpeg"
) : ProcessorStep {

    private lateinit var finalImage: Bitmap
    private lateinit var frameToCropTransform: Matrix
    private lateinit var cropToFrameTransform: Matrix
    private val artisticStyleTransfer = ArtisticStyleTransferImpl(
        context
    ).also {
        it.setStyleImage(styleFilePath)
    }

    override fun getWidthForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.width
    }

    override fun getHeightForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.height
    }

    override suspend fun init(
        videoSourceWidth: Int,
        videoSourceHeight: Int
    ) {
        finalImage = Bitmap.createBitmap(
            videoSourceWidth,
            videoSourceHeight,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform = getTransformationMatrix(
            videoSourceWidth,
            videoSourceHeight,
            ArtisticStyleTransferImpl.CONTENT_IMAGE_SIZE,
            ArtisticStyleTransferImpl.CONTENT_IMAGE_SIZE,
            0,
            false
        )
        cropToFrameTransform = Matrix().also {
            frameToCropTransform.invert(it)
        }
    }

    override fun applyForData(bitmap: Bitmap): Bitmap =
        applyForData(bitmap.toByteArray(), bitmap.width, bitmap.height)

    override fun applyForData(rgbBytes: ByteArray, width: Int, height: Int): Bitmap {
        val result = artisticStyleTransfer.styleTransform(
            rgbBytes,
            width,
            height
        )
        val finalBitmapCanvas = Canvas(finalImage)
        finalBitmapCanvas.drawBitmap(result.toBitmap(), cropToFrameTransform, null)
        return finalImage
    }
}