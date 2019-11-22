package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap
import com.bendenen.visionai.videosource.VideoSourceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class VideoProcessor : VideoSourceListener, CoroutineScope {

    private var videoProcessorListener: VideoProcessorListener? = null
    private val steps = mutableListOf<VideoProcessorStep<out StepConfig>>()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun <T : StepConfig> addStep(videoProcessorStep: VideoProcessorStep<T>) {
        steps.add(videoProcessorStep)
    }

    fun <T : StepConfig> setSteps(videoProcessorSteps: List<VideoProcessorStep<T>>) {
        this.steps.clear()
        this.steps.addAll(videoProcessorSteps)
    }

    internal fun setListener(videoProcessorListener: VideoProcessorListener) {
        this.videoProcessorListener = videoProcessorListener
    }

    @Suppress("UNCHECKED_CAST")
    fun getStepAtPosition(position: Int): VideoProcessorStep<StepConfig> =
        steps[position] as VideoProcessorStep<StepConfig>

    suspend fun applyForData(
        bitmap: Bitmap
    ): Bitmap = withContext(coroutineContext) {
        var result = bitmap
        for (step in steps) {
            result = step.applyForData(result)
        }

        return@withContext result
    }

    override fun onNewData(rgbBytes: ByteArray, bitmap: Bitmap) {
        var result = steps[0].applyForData(bitmap)

        for (index in 1 until steps.size) {
            result = steps[index].applyForData(result)
        }
        videoProcessorListener?.onNewFrameProcessed(result)
    }

    override fun onNewFrame(rgbBytes: ByteArray) {
        // TODO: Implement for data
    }

    override fun onNewBitmap(bitmap: Bitmap) {
        // TODO: Implement for data
    }

    override fun onFinish() {
        videoProcessorListener?.onFinish()
    }
}