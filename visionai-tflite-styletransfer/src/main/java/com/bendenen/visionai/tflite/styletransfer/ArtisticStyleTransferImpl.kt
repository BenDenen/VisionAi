package com.bendenen.visionai.tflite.styletransfer

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.bendenen.visionai.visionai.shared.utils.NativeImageUtilsWrapper
import com.bendenen.visionai.visionai.shared.utils.loadModelFile
import com.bendenen.visionai.visionai.shared.utils.toNormalizedFloatByteBuffer
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ArtisticStyleTransferImpl(
    context: Context
) : ArtisticStyleTransfer {

    private var assetManager = context.assets

    private val contentImageTfLite: Interpreter by lazy {
        Interpreter(
            assetManager.loadModelFile(
                STYLE_TRANSFER_FILE_NAME
            ),
            Interpreter.Options()
        )
    }

    private lateinit var bottleNeckBuffer: Array<Array<Array<FloatArray>>>
    private lateinit var styleImagePath: String
    private var updateBottleNeck = true

    private val contentImageFloatArrayBuffer by lazy {
        FloatArray(1 * CONTENT_IMAGE_SIZE * CONTENT_IMAGE_SIZE * CHANNELS_NUM)
    }
    private val contentImageDataBuffer by lazy {
        ByteBuffer.allocateDirect(
            1 * CONTENT_IMAGE_SIZE * CONTENT_IMAGE_SIZE * CHANNELS_NUM * 4
        ).also {
            it.order(ByteOrder.nativeOrder())
        }
    }
    private val finalImageData by lazy {
        Array(1) {
            Array(CONTENT_IMAGE_SIZE) {
                Array(CONTENT_IMAGE_SIZE) {
                    FloatArray(
                        CHANNELS_NUM
                    )
                }
            }
        }
    }

    override fun setStyle(style: Style) {
        when (style) {
            is Style.AssetStyle -> styleImagePath = style.styleFileName
            else -> throw IllegalArgumentException("Unknown style type ${style.javaClass.simpleName}")
        }

        updateBottleNeck = true
        bottleNeckBuffer =
            Array(BOTTLE_NECK_SIZE[0]) {
                Array(BOTTLE_NECK_SIZE[1]) {
                    Array(BOTTLE_NECK_SIZE[2]) {
                        FloatArray(
                            BOTTLE_NECK_SIZE[3]
                        )
                    }
                }
            }
    }

    override fun styleTransform(
        contentImageData: ByteArray,
        imageWidth: Int,
        imageHeight: Int
    ): Array<Array<Array<FloatArray>>> {

        Log.d(TAG, " Start ")
        val startTime = System.currentTimeMillis()

        if (updateBottleNeck) {
            // TODO: Check all available storage of Style images
            val bitmap = BitmapFactory.decodeStream(assetManager.open(styleImagePath))

            val imageStyleData =
                ByteBuffer.allocateDirect(1 * STYLE_IMAGE_SIZE * STYLE_IMAGE_SIZE * CHANNELS_NUM * 4)
            imageStyleData.order(ByteOrder.nativeOrder())
            bitmap.toNormalizedFloatByteBuffer(
                imageStyleData,
                STYLE_IMAGE_SIZE,
                IMAGE_MEAN
            )
            getStyleImageTfLite().run(imageStyleData, bottleNeckBuffer)
            updateBottleNeck = false
        }

        NativeImageUtilsWrapper.resizeAndNormalizeImage(
            contentImageData,
            imageWidth,
            imageHeight,
            CONTENT_IMAGE_SIZE,
            CONTENT_IMAGE_SIZE,
            contentImageFloatArrayBuffer
        )

        contentImageDataBuffer.rewind()
        contentImageFloatArrayBuffer.forEach {
            contentImageDataBuffer.putFloat(it)
        }

        val inputArray = arrayOf(contentImageDataBuffer, bottleNeckBuffer)
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = finalImageData

        Log.d(TAG, " Prepearing " + (System.currentTimeMillis() - startTime))

        val middleTime = System.currentTimeMillis()

        contentImageTfLite.runForMultipleInputsOutputs(inputArray, outputMap)

        Log.d(TAG, " Run " + (System.currentTimeMillis() - middleTime))

        return finalImageData
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getStyleImageTfLite() = Interpreter(
        assetManager.loadModelFile(
            STYLE_PREDICT_FILE_NAME
        ),
        Interpreter.Options()
    )

    companion object {

        private const val TAG = "ArtisticStyleTransfer"

        const val STYLE_IMAGE_SIZE = 256
        const val CONTENT_IMAGE_SIZE = 384
        const val CHANNELS_NUM = 3

        private val BOTTLE_NECK_SIZE = arrayOf(1, 1, 1, 100)

        private const val STYLE_PREDICT_FILE_NAME = "style_predict_quantized_256.tflite"
        private const val STYLE_TRANSFER_FILE_NAME = "style_transfer_quantized_dynamic.tflite"

        private const val IMAGE_MEAN = 255.0f
    }
}