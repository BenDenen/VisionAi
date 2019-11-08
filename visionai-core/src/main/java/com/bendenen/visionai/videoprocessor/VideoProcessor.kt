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
    private val steps = mutableListOf<ProcessorStep>()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun addStep(processorStep: ProcessorStep) {
        steps.add(processorStep)
    }

    fun setSteps(processorSteps: List<ProcessorStep>) {
        steps.clear()
        steps.addAll(processorSteps)
    }

    internal fun setListener(videoProcessorListener: VideoProcessorListener) {
        this.videoProcessorListener = videoProcessorListener
    }

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