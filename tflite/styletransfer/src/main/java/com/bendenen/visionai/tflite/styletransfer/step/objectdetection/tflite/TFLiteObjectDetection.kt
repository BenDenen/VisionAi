package com.bendenen.visionai.tflite.styletransfer.step.objectdetection.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Trace
import com.bendenen.visionai.tflite.styletransfer.step.objectdetection.ObjectDetector
import com.bendenen.visionai.visionai.shared.utils.loadModelFile
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Vector

class TFLiteObjectDetection private constructor() :
    ObjectDetector {

    private var isModelQuantized: Boolean = false
    private var inputSize: Int = 0
    private val labels = Vector<String>()
    private var intValues: IntArray? = null
    private var outputLocations: Array<Array<FloatArray>>? = null
    private var outputClasses: Array<FloatArray>? = null
    private var outputScores: Array<FloatArray>? = null
    private var numDetections: FloatArray? = null

    private var imgData: ByteBuffer? = null

    private lateinit var tfLite: Interpreter

    override val statString: String
        get() = ""

    override fun recognizeImageBytes(bytes: ByteArray): List<ObjectDetector.Recognition> {

        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")

        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        numDetections = FloatArray(1)

        imgData!!.rewind()
        imgData!!.put(bytes)
        val inputArray = arrayOf<Any>(imgData!!)
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputLocations!!
        outputMap[1] = outputClasses!!
        outputMap[2] = outputScores!!
        outputMap[3] = numDetections!!
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()

        // Show the best detections.
        // after scaling them back to the input size.
        val recognitions = ArrayList<ObjectDetector.Recognition>(
            NUM_DETECTIONS
        )
        for (i in 0 until NUM_DETECTIONS) {
            val detection = RectF(
                outputLocations!![0][i][1] * inputSize,
                outputLocations!![0][i][0] * inputSize,
                outputLocations!![0][i][3] * inputSize,
                outputLocations!![0][i][2] * inputSize
            )
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            val labelOffset = 1
            recognitions.add(
                ObjectDetector.Recognition(
                    "" + i,
                    labels[outputClasses!![0][i].toInt() + labelOffset],
                    outputScores!![0][i],
                    detection
                )
            )
        }
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun recognizeImage(bitmap: Bitmap): List<ObjectDetector.Recognition> {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage")

        Trace.beginSection("preprocessBitmap")
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        imgData!!.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues!![i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData!!.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData!!.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData!!.put((pixelValue and 0xFF).toByte())
                } else { // Float model
                    imgData!!.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        Trace.endSection() // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed")
        outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        numDetections = FloatArray(1)

        val inputArray = arrayOf<Any>(imgData!!)
        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputLocations!!
        outputMap[1] = outputClasses!!
        outputMap[2] = outputScores!!
        outputMap[3] = numDetections!!
        Trace.endSection()

        // Run the inference call.
        Trace.beginSection("run")
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        Trace.endSection()

        // Show the best detections.
        // after scaling them back to the input size.
        val recognitions = ArrayList<ObjectDetector.Recognition>(
            NUM_DETECTIONS
        )
        for (i in 0 until NUM_DETECTIONS) {
            val detection = RectF(
                outputLocations!![0][i][1] * inputSize,
                outputLocations!![0][i][0] * inputSize,
                outputLocations!![0][i][3] * inputSize,
                outputLocations!![0][i][2] * inputSize
            )
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            val labelOffset = 1
            recognitions.add(
                ObjectDetector.Recognition(
                    "" + i,
                    labels[outputClasses!![0][i].toInt() + labelOffset],
                    outputScores!![0][i],
                    detection
                )
            )
        }
        Trace.endSection() // "recognizeImage"
        return recognitions
    }

    override fun enableStatLogging(logStats: Boolean) {}

    override fun close() {}

    override fun setNumThreads(num_threads: Int) {
        if (tfLite != null) tfLite!!.setNumThreads(num_threads)
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        if (tfLite != null) tfLite!!.setUseNNAPI(isChecked)
    }

    companion object {

        // Only return this many results.
        private val NUM_DETECTIONS = 10
        // Float model
        private const val IMAGE_MEAN = 128.0f
        private const val IMAGE_STD = 128.0f
        // Number of threads in the java app
        private const val NUM_THREADS = 4

        /**
         * Initializes a native TensorFlow session for classifying images.
         *
         * @param assetManager The asset manager to be used to load assets.
         * @param modelFilename The filepath of the model GraphDef protocol buffer.
         * @param labelFilename The filepath of label file for classes.
         * @param inputSize The size of image input
         * @param isQuantized Boolean representing model is quantized or not
         */
        @Throws(IOException::class)
        fun create(
            assetManager: AssetManager,
            modelFilename: String,
            labelFilename: String,
            inputSize: Int,
            isQuantized: Boolean
        ): ObjectDetector {
            val d =
                TFLiteObjectDetection()

            var labelsInput: InputStream? = null
            val actualFilename =
                labelFilename.split("file:///android_asset/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            labelsInput = assetManager.open(actualFilename)
            var br: BufferedReader? = null
            br = BufferedReader(InputStreamReader(labelsInput))
            var line: String? = null
            while ({ line = br.readLine(); line }() != null) {
                d.labels.add(line)
            }
            br.close()

            d.inputSize = inputSize

            try {
                d.tfLite = Interpreter(
                    assetManager.loadModelFile(
                        modelFilename
                    ),
                    Interpreter.Options().setUseNNAPI(true)
                )
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

            d.isModelQuantized = isQuantized
            // Pre-allocate buffers.
            val numBytesPerChannel: Int
            if (isQuantized) {
                numBytesPerChannel = 1 // Quantized
            } else {
                numBytesPerChannel = 4 // Floating point
            }
            d.imgData =
                ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel)
            d.imgData!!.order(ByteOrder.nativeOrder())
            d.intValues = IntArray(d.inputSize * d.inputSize)

            d.tfLite!!.setNumThreads(NUM_THREADS)
            d.outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
            d.outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
            d.outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
            d.numDetections = FloatArray(1)
            return d
        }
    }
}
