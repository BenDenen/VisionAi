package com.bendenen.visionai.videoprocessor

import android.graphics.Bitmap

abstract class VideoProcessorStep<T>(
    protected val stepConfig: T
) where T : StepConfig {

    abstract fun getWidthForNextStep(): Int

    abstract fun getHeightForNextStep(): Int

    protected abstract suspend fun initWithConfig()

    protected abstract suspend fun updateWithConfig(
        newConfig: T
    )

    internal fun getConfig() = stepConfig

    internal suspend fun init() = initWithConfig()

    internal suspend fun updateConfig(stepConfig: T) {
        stepConfig.videoSourceWidth = this.stepConfig.videoSourceWidth
        stepConfig.videoSourceHeight = this.stepConfig.videoSourceHeight
        updateWithConfig(stepConfig)
    }

    abstract fun applyForData(
        bitmap: Bitmap
    ): Bitmap
}