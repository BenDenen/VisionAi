package com.bendenen.tfliteexample.videoprocessor.tflite

import android.app.Application
import android.graphics.*
import android.os.SystemClock
import android.util.Size
import com.bendenen.tfliteexample.ml.Classifier
import com.bendenen.tfliteexample.ml.tflite.TFLiteObjectDetectionAPIModel
import com.bendenen.tfliteexample.utils.Logger
import com.bendenen.tfliteexample.utils.getTransformationMatrix
import com.bendenen.tfliteexample.utils.tracking.MultiBoxTracker
import com.bendenen.tfliteexample.video.VideoSource
import com.bendenen.tfliteexample.video.VideoSourceListener
import com.bendenen.tfliteexample.video.mediacodec.MediaCodecVideoSourceImpl
import com.bendenen.tfliteexample.videoprocessor.VideoProcessor
import com.bendenen.tfliteexample.videoprocessor.VideoProcessorListener
import java.util.*

class TfLiteVideoProcessorImpl(
    private val application: Application,
    private val requestedWidth: Int,
    private val requestedHeight: Int,
    override var videoProcessorListener: VideoProcessorListener?
) : VideoProcessor, VideoSourceListener {

    private val detector: Classifier = TFLiteObjectDetectionAPIModel.create(
        application.assets,
        TF_OD_API_MODEL_FILE,
        TF_OD_API_LABELS_FILE,
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_IS_QUANTIZED
    )
    private val videoSource: VideoSource = MediaCodecVideoSourceImpl(
        application,
        requestedWidth,
        requestedHeight
    )
    private var rgbFrameBitmap = Bitmap.createBitmap(
        videoSource.getSourceWidth(),
        videoSource.getSourceHeight(),
        Bitmap.Config.ARGB_8888
    );
    private var croppedBitmap = Bitmap.createBitmap(
        TF_OD_API_INPUT_SIZE,
        TF_OD_API_INPUT_SIZE,
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
    private var tracker = MultiBoxTracker(application)

    private var timestamp: Long = 0
    private var lastProcessingTimeMs: Long = 0


    override fun start() {
        videoSource.useBitmap(true)
        videoSource.attach(this)
    }

    override fun stop() {
        videoSource.detach()
    }

    override fun onNewFrame(rgbBytes: ByteArray) {

    }

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
        paint.strokeWidth = 2.0f

        var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
        when (MODE) {
           DetectorMode.TF_OD_API -> minimumConfidence =
                MINIMUM_CONFIDENCE_TF_OD_API
        }

        val mappedRecognitions = LinkedList<Classifier.Recognition>()

        for (result in results) {
            val location = result.getLocation()
            if (location != null && result.confidence >= minimumConfidence) {


                cropToFrameTransform.mapRect(location)

                croppedBitmapCanvas.drawRect(location, paint)
//
//                result.setLocation(location)
//                mappedRecognitions.add(result)
            }
        }

        videoProcessorListener?.onNewFrameProcessed(finalBitmap)
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }

    companion object {

        private val LOGGER = Logger();

        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE = DetectorMode.TF_OD_API

        private val MINIMUM_CONFIDENCE_TF_OD_API = 0.3f
        private val MAINTAIN_ASPECT = false
        private val DESIRED_PREVIEW_SIZE = Size(640, 480)
        private val SAVE_PREVIEW_BITMAP = false
        private val TEXT_SIZE_DIP = 10f
    }
}