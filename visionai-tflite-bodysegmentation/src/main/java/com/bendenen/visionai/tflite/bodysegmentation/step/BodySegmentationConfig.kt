package com.bendenen.visionai.tflite.bodysegmentation.step

import android.content.Context
import com.bendenen.visionai.videoprocessor.StepConfig

data class BodySegmentationConfig(
    val context: Context,
    val segmentationModes: List<SegmentationMode>
) : StepConfig()

enum class SegmentationMode(val classIndex: Int) {
    BACKGROUND(0),
    PERSON(15),
}