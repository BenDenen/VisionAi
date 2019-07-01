package com.bendenen.tfliteexample.videoprocessor.tflite

import android.app.Application
import android.graphics.Bitmap
import android.util.Size
import com.bendenen.tfliteexample.ml.Classifier
import com.bendenen.tfliteexample.ml.tflite.TFLiteObjectDetectionAPIModel
import com.bendenen.tfliteexample.video.VideoSource
import com.bendenen.tfliteexample.video.VideoSourceListener
import com.bendenen.tfliteexample.video.mediacodec.MediaCodecVideoSourceImpl
import com.bendenen.tfliteexample.videoprocessor.VideoProcessor
import com.bendenen.tfliteexample.videoprocessor.VideoProcessorListener

class TfLiteVideoProcessorImpl(
    private val application: Application,
    private val requestedWidth: Int,
    private val requestedHeight: Int,
    override var videoProcessorListener: VideoProcessorListener?
) : VideoProcessor, VideoSourceListener {

    companion object {
        private const val TF_OD_API_INPUT_SIZE = 300
        private const val TF_OD_API_IS_QUANTIZED = true
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE =
            DetectorMode.TF_OD_API


        private val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
        private val MAINTAIN_ASPECT = false
        private val DESIRED_PREVIEW_SIZE = Size(640, 480)
        private val SAVE_PREVIEW_BITMAP = false
        private val TEXT_SIZE_DIP = 10f
    }

    private var detector: Classifier? = null
    private val videoSource: VideoSource =
        MediaCodecVideoSourceImpl(
            application, requestedWidth, requestedHeight
        )

    init {
        detector = TFLiteObjectDetectionAPIModel.create(
            application.assets,
            TF_OD_API_MODEL_FILE,
            TF_OD_API_LABELS_FILE,
            TF_OD_API_INPUT_SIZE,
            TF_OD_API_IS_QUANTIZED
        )
    }


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
        videoProcessorListener?.onNewFrameProcessed(bitmap)
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }
}