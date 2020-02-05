package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

abstract class VideoProcessorStep<C, R>(
    protected val stepConfig: C
) where C : StepConfig, R: StepResult<out Any> {

    protected abstract fun getWidthForNextStep(): Int

    internal fun getNextStepWidth() = getWidthForNextStep()

    protected abstract fun getHeightForNextStep(): Int

    internal fun getNextStepHeight() = getHeightForNextStep()

    protected abstract suspend fun initWithConfig()

    protected abstract suspend fun updateWithConfig(
        newConfig: C
    )

    internal fun getConfig() = stepConfig

    internal suspend fun init() = initWithConfig()

    internal suspend fun updateConfig(stepConfig: C) {
        stepConfig.videoSourceWidth = this.stepConfig.videoSourceWidth
        stepConfig.videoSourceHeight = this.stepConfig.videoSourceHeight
        updateWithConfig(stepConfig)
    }

    abstract suspend fun applyForData(
        bitmap: Bitmap
    ): R
}