package com.bendenen.visionai.tflite.styletransfer.step

import android.content.Context
import android.graphics.BlendMode
import com.bendenen.visionai.videoprocessor.StepConfig

data class StyleTransferConfig(
    var context: Context,
    var style: Style,
    var blendMode: StyleTransferBlendMode = StyleTransferBlendMode.OFF,
    var sourcesOrder: SourcesOrder = SourcesOrder.FORWARD,
    var maskAlpha: Int = 255

) : StepConfig()

enum class StyleTransferBlendMode(val blendMode: BlendMode?) {

    OFF(null),
    PLUS(BlendMode.PLUS),
    MODULATE(BlendMode.MODULATE),
    SCREEN(BlendMode.SCREEN),
    OVERLAY(BlendMode.SCREEN),
    DARKEN(BlendMode.DARKEN),
    LIGHTEN(BlendMode.LIGHTEN),
    COLOR_DODGE(BlendMode.COLOR_DODGE),
    COLOR_BURN(BlendMode.COLOR_BURN),
    HARD_LIGHT(BlendMode.HARD_LIGHT),
    SOFT_LIGHT(BlendMode.SOFT_LIGHT),
    DIFFERENCE(BlendMode.DIFFERENCE),
    EXCLUSION(BlendMode.EXCLUSION),
    MULTIPLY(BlendMode.MULTIPLY),
    HUE(BlendMode.HUE),
    SATURATION(BlendMode.SATURATION),
    COLOR(BlendMode.COLOR),
    LUMINOSITY((BlendMode.LUMINOSITY));
}