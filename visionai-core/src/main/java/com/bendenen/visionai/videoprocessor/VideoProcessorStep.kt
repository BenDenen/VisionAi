package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

abstract class VideoProcessorStep<C, R>(
    protected val stepConfig: C
) where C : StepConfig, R: StepResult<out Any> {

    abstract fun getWidthForNextStep(): Int

    abstract fun getHeightForNextStep(): Int

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

    abstract fun applyForData(
        bitmap: Bitmap
    ): R
}