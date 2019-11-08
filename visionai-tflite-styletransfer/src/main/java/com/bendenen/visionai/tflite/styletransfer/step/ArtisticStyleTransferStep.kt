package com.bendenen.visionai.tflite.styletransfer.step

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bendenen.visionai.tflite.styletransfer.ArtisticStyleTransferImpl
import com.bendenen.visionai.videoprocessor.ProcessorStep
import com.bendenen.visionai.visionai.shared.utils.getTransformationMatrix
import com.bendenen.visionai.visionai.shared.utils.toBitmap
import com.bendenen.visionai.visionai.shared.utils.toByteArray

class ArtisticStyleTransferStep(
    context: Context,
    style: Style,
    private val blendMode: BlendMode? = null,
    private val sourcesOrder: SourcesOrder = SourcesOrder.FORWARD
) : ProcessorStep {

    private lateinit var finalImage: Bitmap
    private var croppedImage = Bitmap.createBitmap(
        ArtisticStyleTransferImpl.CONTENT_IMAGE_SIZE,
        ArtisticStyleTransferImpl.CONTENT_IMAGE_SIZE,
        Bitmap.Config.ARGB_8888
    )
    private lateinit var frameToCropTransform: Matrix
    private lateinit var cropToFrameTransform: Matrix
    private val artisticStyleTransfer = ArtisticStyleTransferImpl(
        context
    ).also {
        when (style) {
            is Style.AssetStyle -> {

                val styleBitmap =
                    BitmapFactory.decodeStream(context.assets.open(style.styleFileName))
                it.setStyle(styleBitmap)
            }
        }

    }

    override fun getWidthForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.width
    }

    override fun getHeightForNextStep(): Int {
        assert(::finalImage.isInitialized)
        return finalImage.height
    }

    override fun init(
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

    override fun applyForData(bitmap: Bitmap): Bitmap {

        val croppedCanvas = Canvas(croppedImage)
        croppedCanvas.drawBitmap(bitmap, frameToCropTransform, null)

        val styledBitmap =
            applyForData(croppedImage.toByteArray(), croppedImage.width, croppedImage.height)

        val finalBitmapCanvas = Canvas(finalImage)

        val blendMode = blendMode
        if (blendMode != null) {
            when (sourcesOrder) {
                SourcesOrder.FORWARD -> {
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), null)
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, Paint().also {
                        it.blendMode = blendMode
                    })
                }
                SourcesOrder.BACKWARD -> {
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, null)
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), Paint().also {
                        it.blendMode = blendMode
                    })
                }
            }
        } else {
            finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, null)
        }



        return finalImage
    }

    private fun applyForData(rgbBytes: ByteArray, width: Int, height: Int): Bitmap {
        val result = artisticStyleTransfer.styleTransform(
            rgbBytes,
            width,
            height
        )
        return result.toBitmap()
    }
}