package com.bendenen.visionai

import android.graphics.Bitmap
import android.util.Log
import com.bendenen.visionai.outputencoder.OutputEncoder
import com.bendenen.visionai.outputencoder.mediamuxer.MediaMuxerOutputEncoderImpl
import com.bendenen.visionai.videoprocessor.StepConfig
import com.bendenen.visionai.videoprocessor.StepResult
import com.bendenen.visionai.videoprocessor.VideoProcessor
import com.bendenen.visionai.videoprocessor.VideoProcessorListener
import com.bendenen.visionai.videoprocessor.VideoProcessorStep
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
        fun onStepsResult(bitmap: Bitmap)
        fun onFileResult(filePath: String)
    }

    interface VideoProcessorStepListener {
        fun onStepResult(stepIndex: Int, sourceBitmap: Bitmap, stepResult: StepResult<*>)
    }

    val TAG = VisionAi::class.java.simpleName

    private lateinit var videoProcessor: VideoProcessor

    private lateinit var videoSource: VideoSource
    private lateinit var outputEncoder: OutputEncoder

    private var resultListener: ResultListener? = null
    private var videoProcessorStepListener: VideoProcessorStepListener? = null

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    suspend fun init(
        visionAiConfig: VisionAiConfig
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
        } else if (visionAiConfig.videoUri != null && visionAiConfig.application != null) {

            videoSource = MediaCodecVideoSourceImpl(visionAiConfig.application)
            (videoSource as MediaFileVideoSource).loadVideoFile(
                visionAiConfig.videoUri
            )
            initOutputEncoder()
        } else {
            throw IllegalArgumentException(" Not enough information about video source ")
        }
        videoSource.useBitmap(true)
        videoProcessor.setListener(VisionAi)
    }

    fun setVideoProcessorStepListener(videoProcessorStepListener: VideoProcessorStepListener) {
        this.videoProcessorStepListener = videoProcessorStepListener
    }

    suspend fun <C : StepConfig, R : StepResult<out Any>> initSteps(
        videoProcessorSteps: List<VideoProcessorStep<C, R>>
    ) {
        assert(::videoProcessor.isInitialized)
        assert(::videoSource.isInitialized)

        var width = videoSource.getSourceWidth()
        var height = videoSource.getSourceHeight()

        for (step in videoProcessorSteps) {
            step.getConfig().apply {
                videoSourceWidth = width
                videoSourceHeight = height
            }
            step.init()
            width = step.getWidthForNextStep()
            height = step.getHeightForNextStep()
        }

        videoProcessor.setSteps(videoProcessorSteps)
    }

    suspend fun updateConfigForStep(stepIndex: Int, config: StepConfig) {
        assert(::videoProcessor.isInitialized)
        assert(::videoSource.isInitialized)
        assert(stepIndex >= 0)

        videoProcessor.getStepAtPosition(stepIndex).updateConfig(config)
    }

    @Throws(IllegalArgumentException::class, AssertionError::class)
    suspend fun requestPreview(
        timestamp: Long
    ): StepResult<out Any> {
        assert(::videoSource.isInitialized)
        assert(::videoProcessor.isInitialized)
        require(videoSource is MediaFileVideoSource)
        val bitmap = (videoSource as MediaFileVideoSource).requestPreview(timestamp)
        return videoProcessor.applyForData(bitmap)
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

    override fun onStepResult(stepIndex: Int, sourceBitmap: Bitmap, stepResult: StepResult<*>) {
        videoProcessorStepListener?.onStepResult(stepIndex, sourceBitmap, stepResult)
    }

    override fun onNewFrameProcessed(stepResult: StepResult<*>) {
        outputEncoder.encodeBitmap(Bitmap.createBitmap(stepResult.getBitmapForNextStep()))
        resultListener?.let {
            launch {
                it.onStepsResult(stepResult.getBitmapForNextStep())
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