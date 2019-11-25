package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

interface VideoProcessorListener {

    fun onStepResult(stepIndex:Int, sourceBitmap:Bitmap, stepResult: StepResult<*>)

    fun onNewFrameProcessed(stepResult: StepResult<*>)

    fun onFinish()
}