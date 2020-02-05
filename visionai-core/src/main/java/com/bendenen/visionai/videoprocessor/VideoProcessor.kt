package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap
import com.bendenen.visionai.videosource.VideoSourceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class VideoProcessor : VideoSourceListener, CoroutineScope {

    private var videoProcessorListener: VideoProcessorListener? = null
    private val steps = mutableListOf<VideoProcessorStep<out StepConfig, out StepResult<out Any>>>()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun <T : StepConfig> addStep(videoProcessorStep: VideoProcessorStep<T, StepResult<out Any>>) {
        steps.add(videoProcessorStep)
    }

    fun <C : StepConfig, R : StepResult<out Any>> setSteps(
        videoProcessorSteps: List<VideoProcessorStep<C, R>>
    ) {
        this.steps.clear()
        this.steps.addAll(videoProcessorSteps)
    }

    internal fun setListener(videoProcessorListener: VideoProcessorListener) {
        this.videoProcessorListener = videoProcessorListener
    }

    @Suppress("UNCHECKED_CAST")
    fun getStepAtPosition(position: Int): VideoProcessorStep<StepConfig, StepResult<out Any>> =
        steps[position] as VideoProcessorStep<StepConfig, StepResult<out Any>>

    suspend fun applyForData(
        bitmap: Bitmap
    ): StepResult<out Any> = withContext(coroutineContext) {
        var result: StepResult<out Any> = object : StepResult<Bitmap> {
            override fun getSourceBitmap(): Bitmap = bitmap

            override fun getResult(): Bitmap = bitmap

            override fun getBitmapForNextStep(): Bitmap = bitmap
        }
        for (step in steps) {
            result = step.applyForData(result.getBitmapForNextStep())
        }

        return@withContext result
    }

    override fun onNewFrame(rgbBytes: ByteArray) {
        // TODO: Implement for data
    }

    override fun onNewBitmap(bitmap: Bitmap) {
        launch {
            var result = steps[0].applyForData(bitmap)
            videoProcessorListener?.onStepResult(0, bitmap, result)

            for (index in 1 until steps.size) {
                result = steps[index].applyForData(result.getBitmapForNextStep())
            }
            videoProcessorListener?.onNewFrameProcessed(result)
        }
    }

    override fun onFinish() {
        videoProcessorListener?.onFinish()
    }
}