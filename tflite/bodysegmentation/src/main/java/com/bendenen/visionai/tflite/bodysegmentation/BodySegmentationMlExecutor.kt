package com.bendenen.visionai.tflite.bodysegmentation

import android.content.Context
import android.util.Log
import com.bendenen.visionai.visionai.shared.utils.loadModelFile
import com.bendenen.visionai.visionai.shared.utils.toNormalizedFloatByteBuffer
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface BodySegmentationMlExecutor {

    fun segment(
        contentImageData: ByteArray,
        imageWidth: Int,
        imageHeight: Int
    ): Array<Array<Array<FloatArray>>>

    fun getContentImageSize(): Int

    fun close()

    class Impl(context: Context) : BodySegmentationMlExecutor {

        private val deligate = GpuDelegate()

        private val contentImageTfLite: Interpreter by lazy {
            Interpreter(
                context.assets.loadModelFile(
                    MODEL_NAME
                ),
                Interpreter.Options().addDelegate(deligate)
            )
        }

        private val contentImageDataBuffer by lazy {
            ByteBuffer.allocateDirect(
                1 * CONTENT_IMAGE_SIZE * CONTENT_IMAGE_SIZE * CHANNELS_NUM * 4
            ).also {
                it.order(ByteOrder.nativeOrder())
            }
        }

        private val segmentationMask by lazy {
            Array(1) {
                Array(CONTENT_IMAGE_SIZE) {
                    Array(CONTENT_IMAGE_SIZE) {
                        FloatArray(
                            CLASSES_NUM
                        )
                    }
                }
            }
        }

        override fun segment(
            contentImageData: ByteArray,
            imageWidth: Int,
            imageHeight: Int
        ): Array<Array<Array<FloatArray>>> {
            Log.d(TAG, " Start ")
            val startTime = System.currentTimeMillis()

            contentImageData.toNormalizedFloatByteBuffer(contentImageDataBuffer, IMAGE_MEAN)

            val inputArray = arrayOf(contentImageDataBuffer)
            val outputMap = HashMap<Int, Any>()
            outputMap[0] = segmentationMask

            Log.d(TAG, " Prepearing " + (System.currentTimeMillis() - startTime))

            val middleTime = System.currentTimeMillis()

            contentImageTfLite.runForMultipleInputsOutputs(inputArray, outputMap)

            Log.d(TAG, " Run " + (System.currentTimeMillis() - middleTime))

            return segmentationMask
        }

        override fun getContentImageSize(): Int = CONTENT_IMAGE_SIZE

        override fun close() {
            deligate.close()
        }

        companion object {
            const val TAG = "BodySegmentation"

            private const val MODEL_NAME = "segmentation_gpu.tflite"

            private const val CONTENT_IMAGE_SIZE = 257
            private const val CHANNELS_NUM = 3
            private const val CLASSES_NUM = 21

            private const val IMAGE_MEAN = 255.0f
        }
    }
}