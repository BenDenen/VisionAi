package com.bendenen.visionai.videoprocessor.tflite

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_MOVIES
import android.os.SystemClock
import android.util.Log
import android.util.Size
import com.bendenen.visionai.ml.objectdetection.ObjectDetector
import com.bendenen.visionai.ml.objectdetection.tflite.TFLiteObjectDetection
import com.bendenen.visionai.ml.styletransfer.tflite.TFLiteArtisticStyleTransfer
import com.bendenen.visionai.utils.Logger
import com.bendenen.visionai.utils.getTransformationMatrix
import com.bendenen.visionai.utils.toBitmap
import com.bendenen.visionai.utils.tracking.MultiBoxTracker
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener
import com.bendenen.visionai.videoprocessor.outputencoder.OutputEncoder
import com.bendenen.visionai.videoprocessor.outputencoder.mediamuxer.MediaMuxerOutputEncoderImpl
import com.bendenen.visionai.videosource.VideoSource
import com.bendenen.visionai.videosource.VideoSourceListener
import com.bendenen.visionai.videosource.mediacodec.MediaCodecVideoSourceImpl
import java.io.File
import java.util.LinkedList

class TfLiteVideoProcessorImpl(
    private val application: Application,
    private val requestedWidth: Int,
    private val requestedHeight: Int,
    override var videoProcessorListener: VideoProcessorListener?,
    val videoUri: Uri
) : VideoProcessor, VideoSourceListener {

    private val detector: ObjectDetector = TFLiteObjectDetection.create(
        application.assets,
        TF_OD_API_MODEL_FILE,
        TF_OD_API_LABELS_FILE,
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_IS_QUANTIZED
    )

    private val artisticStyleTransfer = TFLiteArtisticStyleTransfer(application).also {
        it.setStyleImage("test_style.jpeg")
    }

    private val videoSource: VideoSource = MediaCodecVideoSourceImpl(
        application,
        requestedWidth,
        requestedHeight,
        videoUri
    )
    private var croppedBitmap = Bitmap.createBitmap(
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_INPUT_SIZE,
        Bitmap.Config.ARGB_8888
    )
    private var finalImage = Bitmap.createBitmap(
        videoSource.getSourceWidth(),
        videoSource.getSourceHeight(),
        Bitmap.Config.ARGB_8888
    )
    private var frameToCropTransform = getTransformationMatrix(
        videoSource.getSourceWidth(),
        videoSource.getSourceHeight(),
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_INPUT_SIZE,
        0,
        MAINTAIN_ASPECT
    )
    private var cropToFrameTransform = Matrix().also {
        frameToCropTransform.invert(it)
    }

    private val outputEncoder: OutputEncoder = MediaMuxerOutputEncoderImpl(
        videoSource.getSourceWidth(),
        videoSource.getSourceHeight()
    )

    private var tracker = MultiBoxTracker(application)

    private var timestamp: Long = 0
    private var lastProcessingTimeMs: Long = 0

    private val inputByteArrayBuffer = ByteArray(TF_OD_API_INPUT_SIZE * TF_OD_API_INPUT_SIZE * 3)

    override fun start() {
        outputEncoder.initialize(
            File(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES),
                "temp.mp4"
            )
        )
        videoSource.useBitmap(true)
        videoSource.attach(this)
    }

    override fun stop() {
        videoSource.detach()
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

        LOGGER.i("Preparing image $currTimestamp for detection in bg thread.")
//        val canvas = Canvas(croppedBitmap)
//        canvas.drawBitmap(bitmap, frameToCropTransform, null)

        LOGGER.i("Running detection on image $currTimestamp")
        val startTime = SystemClock.uptimeMillis()

        val result = artisticStyleTransfer.styleTransform(
            rgbBytes,
            bitmap.width,
            bitmap.height
        )

//        val results = detector.recognizeImageBytes(inputByteArrayBuffer)

        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

        LOGGER.i("Finish detection on image $lastProcessingTimeMs")

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

        outputEncoder.encodeBitmap(Bitmap.createBitmap(finalImage))

//        videoProcessorListener?.onNewFrameProcessed(finalBitmap)
    }

    override fun onNewFrame(rgbBytes: ByteArray) {
        // TODO:
    }

    var counter = 0
    override fun onNewBitmap(bitmap: Bitmap) {

        ++timestamp
        val currTimestamp = timestamp

        LOGGER.i("Preparing image $currTimestamp for detection in bg thread.")
        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(bitmap, frameToCropTransform, null)

        LOGGER.i("Running detection on image $currTimestamp")
        val startTime = SystemClock.uptimeMillis()

        val results = detector.recognizeImage(croppedBitmap)

        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

        LOGGER.i("Finish detection on image $lastProcessingTimeMs")

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

        outputEncoder.encodeBitmap(finalBitmap)

//        videoProcessorListener?.onNewFrameProcessed(finalBitmap)
    }

    override fun onFinish() {
        outputEncoder.finish()
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

        private val LOGGER = Logger()

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