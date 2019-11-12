package com.bendenen.visionai

import android.graphics.Bitmap
import android.util.Log
import com.bendenen.visionai.outputencoder.OutputEncoder
import com.bendenen.visionai.outputencoder.mediamuxer.MediaMuxerOutputEncoderImpl
import com.bendenen.visionai.videoprocessor.ProcessorStep
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener
import com.bendenen.visionai.videosource.MediaFileVideoSource
import com.bendenen.visionai.videosource.VideoSource
import com.bendenen.visionai.videosource.mediacodec.MediaCodecVideoSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object VisionAi : VideoProcessorListener, CoroutineScope {

    interface ResultListener {
        fun onFrameResult(bitmap: Bitmap)
        fun onFileResult(filePath: String)
    }

    val TAG = VisionAi::class.java.simpleName

    private lateinit var videoProcessor: VideoProcessor

    private lateinit var videoSource: VideoSource
    private lateinit var outputEncoder: OutputEncoder

    private var resultListener: ResultListener? = null

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun init(
        visionAiConfig: VisionAiConfig,
        initHandler: () -> Unit
    ) {

        fun initOutputEncoder() {
            outputEncoder = visionAiConfig.outputEncoder ?: MediaMuxerOutputEncoderImpl()
            outputEncoder.initialize(
                visionAiConfig.outputFile,
                videoSource.getSourceWidth(),
                videoSource.getSourceHeight()
            )
        }

        videoProcessor = VideoProcessor()
        if (visionAiConfig.videoSource != null) {
            videoSource = visionAiConfig.videoSource
            initOutputEncoder()
            initHandler.invoke()
        } else if (visionAiConfig.videoUri != null && visionAiConfig.application != null) {

            videoSource = MediaCodecVideoSourceImpl(visionAiConfig.application)
            launch {
                (videoSource as MediaFileVideoSource).loadVideoFile(
                    visionAiConfig.videoUri
                )
                initOutputEncoder()
                initHandler.invoke()
            }
        } else {
            throw IllegalArgumentException(" Not enough information about video source ")
        }
        videoSource.useBitmap(true)
        videoProcessor.setListener(VisionAi)
    }

    fun setSteps(
        steps: List<ProcessorStep>
    ) {
        assert(::videoProcessor.isInitialized)
        assert(::videoSource.isInitialized)

        var width = videoSource.getSourceWidth()
        var height = videoSource.getSourceHeight()

        for (step in steps) {
            step.init(width, height)
            width = step.getWidthForNextStep()
            height = step.getHeightForNextStep()
        }

        videoProcessor.setSteps(steps)
    }

    @Throws(IllegalArgumentException::class, AssertionError::class)
    fun requestPreview(
        timestamp: Long,
        resultHandler: (Bitmap) -> Unit
    ) {
        assert(::videoSource.isInitialized)
        assert(::videoProcessor.isInitialized)
        require(videoSource is MediaFileVideoSource)
        launch {
            val bitmap = (videoSource as MediaFileVideoSource).requestPreview(timestamp)
            val result = videoProcessor.applyForData(bitmap)
            resultHandler.invoke(result)
        }
    }

    fun start(resultListener: ResultListener) {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        this.resultListener = resultListener
        videoSource.attach(videoProcessor)
    }

    fun stop() {
        if (!::videoProcessor.isInitialized) {
            Log.e(TAG, " videoProcessor is not initialized")
            return
        }
        videoSource.detach()
    }

    fun release() {
        job.cancel()
    }

    override fun onNewFrameProcessed(bitmap: Bitmap) {
        outputEncoder.encodeBitmap(Bitmap.createBitmap(bitmap))
        resultListener?.let {
            launch {
                it.onFrameResult(bitmap)
            }
        }
    }

    override fun onFinish() {
        outputEncoder.finish()
        resultListener?.let {
            launch {
                it.onFileResult(outputEncoder.getFilePath())
            }
        }
    }
}