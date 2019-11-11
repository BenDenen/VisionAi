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
    val config: Config
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
        when (config.style) {
            is Style.AssetStyle -> {

                val styleBitmap =
                    BitmapFactory.decodeStream(context.assets.open(config.style.styleFileName))
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

        val blendMode = config.blendMode
        if (blendMode != null) {
            when (config.sourcesOrder) {
                SourcesOrder.FORWARD -> {
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), null)
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, Paint().also {
                        it.blendMode = blendMode
                        it.alpha = config.maskAlpha
                    })
                }
                SourcesOrder.BACKWARD -> {
                    finalBitmapCanvas.drawBitmap(styledBitmap, cropToFrameTransform, null)
                    finalBitmapCanvas.drawBitmap(bitmap, Matrix(), Paint().also {
                        it.blendMode = blendMode
                        it.alpha = config.maskAlpha
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

    data class Config(
        val style: Style,
        val blendMode: BlendMode? = null,
        val sourcesOrder: SourcesOrder = SourcesOrder.FORWARD,
        val maskAlpha: Int = 255

    )
}