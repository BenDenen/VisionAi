package com.bendenen.visionai.tflite.videoprocessor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Environment
import android.os.Environment.DIRECTORY_MOVIES
import android.os.SystemClock
import android.util.Log
import android.util.Size
import com.bendenen.visionai.tflite.tools.objectdetection.ObjectDetector
import com.bendenen.visionai.tflite.tools.objectdetection.tflite.TFLiteObjectDetection
import com.bendenen.visionai.tflite.tools.styletransfer.tflite.TFLiteArtisticStyleTransfer
import com.bendenen.visionai.tflite.utils.getTransformationMatrix
import com.bendenen.visionai.tflite.utils.toBitmap
import com.bendenen.visionai.videoprocessor.VideoProcessor
import java.io.File
import java.util.LinkedList

class TfLiteVideoProcessorImpl(
    private val application: Application
) : VideoProcessor() {

    private lateinit var detector: ObjectDetector

    private val artisticStyleTransfer = TFLiteArtisticStyleTransfer(application).also {
        it.setStyleImage("test_style.jpeg")
    }

    private lateinit var croppedBitmap: Bitmap

    private lateinit var finalImage: Bitmap

    private lateinit var frameToCropTransform: Matrix

    private lateinit var cropToFrameTransform: Matrix

    private var timestamp: Long = 0
    private var lastProcessingTimeMs: Long = 0

    private val inputByteArrayBuffer = ByteArray(TF_OD_API_INPUT_SIZE * TF_OD_API_INPUT_SIZE * 3)

    override fun init(
        videoSourceWidth: Int,
        videoSourceHeight: Int
    ) {
        detector = TFLiteObjectDetection.create(
            application.assets,
            TF_OD_API_MODEL_FILE,
            TF_OD_API_LABELS_FILE,
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_IS_QUANTIZED
        )
        croppedBitmap = Bitmap.createBitmap(
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE,
            Bitmap.Config.ARGB_8888
        )
        finalImage = Bitmap.createBitmap(
            videoSourceWidth,
            videoSourceHeight,
            Bitmap.Config.ARGB_8888
        )
        frameToCropTransform = getTransformationMatrix(
            videoSourceWidth,
            videoSourceHeight,
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_INPUT_SIZE,
            0,
            MAINTAIN_ASPECT
        )
        cropToFrameTransform = Matrix().also {
            frameToCropTransform.invert(it)
        }
    }

    override fun onNewData(rgbBytes: ByteArray, bitmap: Bitmap) {

//        NativeImageUtilsWrapper.resizeImage(
//            rgbBytes,
//            bitmap.width,
//            bitmap.height,
//            TF_OD_API_INPUT_SIZE,
//            TF_OD_API_INPUT_SIZE,
//            inputByteArrayBuffer
//        )

        ++timestamp
        val currTimestamp = timestamp

//        val canvas = Canvas(croppedBitmap)
//        canvas.drawBitmap(bitmap, frameToCropTransform, null)

        val startTime = SystemClock.uptimeMillis()

        val result = artisticStyleTransfer.styleTransform(
            rgbBytes,
            bitmap.width,
            bitmap.height
        )

//        val results = detector.recognizeImageBytes(inputByteArrayBuffer)

        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

//        val finalBitmap = Bitmap.createBitmap(bitmap)
//        val croppedBitmapCanvas = Canvas(finalBitmap)
//        val paint = Paint()
//        paint.color = Color.RED
//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = 10.0f
//
//        var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
//        when (MODE) {
//            DetectorMode.TF_OD_API -> minimumConfidence =
//                MINIMUM_CONFIDENCE_TF_OD_API
//        }
//
//        // TODO: We will nedd map of mapped recognitions for postprocessing
//        val mappedRecognitions = LinkedList<ObjectDetector.Recognition>()
//
//        for (result in results) {
//            val location = result.getLocation()
//            if (result.confidence >= minimumConfidence) {
//
//                cropToFrameTransform.mapRect(location)
//
//                croppedBitmapCanvas.drawRect(location, paint)
////
////                result.setLocation(location)
////                mappedRecognitions.add(result)
//            }
//        }

        val finalBitmapCanvas = Canvas(finalImage)
        finalBitmapCanvas.drawBitmap(result.toBitmap(), cropToFrameTransform, null)

        videoProcessorListener?.onNewFrameProcessed(finalImage)

    }

    override fun onNewFrame(rgbBytes: ByteArray) {
        // TODO:
    }

    var counter = 0
    override fun onNewBitmap(bitmap: Bitmap) {

        ++timestamp
        val currTimestamp = timestamp

        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(bitmap, frameToCropTransform, null)

        val startTime = SystemClock.uptimeMillis()

        val results = detector.recognizeImage(croppedBitmap)

        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

        val finalBitmap = Bitmap.createBitmap(bitmap)
        val croppedBitmapCanvas = Canvas(finalBitmap)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.0f

        var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
        when (MODE) {
            DetectorMode.TF_OD_API -> minimumConfidence =
                MINIMUM_CONFIDENCE_TF_OD_API
        }

        // TODO: We will nedd map of mapped recognitions for postprocessing
        val mappedRecognitions = LinkedList<ObjectDetector.Recognition>()

        for (result in results) {
            val location = result.getLocation()
            if (result.confidence >= minimumConfidence) {

                cropToFrameTransform.mapRect(location)

                croppedBitmapCanvas.drawRect(location, paint)
//
//                result.setLocation(location)
//                mappedRecognitions.add(result)
            }
        }

        videoProcessorListener?.onNewFrameProcessed(finalBitmap)

    }

    override fun onFinish() {
        videoProcessorListener?.onFinish()
        Log.e(
            "MyTag",
            File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES),
                "temp.mp4"
            ).absolutePath
        )
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }

    companion object {

        private const val TF_OD_API_INPUT_SIZE = 384
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE = DetectorMode.TF_OD_API

        private val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
        private val MAINTAIN_ASPECT = false
        private val DESIRED_PREVIEW_SIZE = Size(640, 480)
        private val SAVE_PREVIEW_BITMAP = false
        private val TEXT_SIZE_DIP = 10f
    }
}