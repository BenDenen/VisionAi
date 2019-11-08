package com.bendenen.visionai.tflite.styletransfer

import android.graphics.BlendMode

sealed class Style {

    abstract var blendMode: BlendMode?

    data class AssetStyle(
        val styleFileName: String,
        override var blendMode: BlendMode? = null
    ) : Style()

}

enum class Gallery(
    val styleName: String,
    val style: Style
) {
    PICASSO_SELF_PORTRAIT(
        "Picasso Self Portrait",
        Style.AssetStyle("picasso_self.jpg", BlendMode.MULTIPLY)
    ),
    SUNFLOWERS(
        "Sunflowers",
        Style.AssetStyle("sunflowers.jpg", BlendMode.SOFT_LIGHT)
    ),
    HAPPY_MOOD_SPRING(
        "Happy Mood Spring",
        Style.AssetStyle("happy_mood_spring.jpg", BlendMode.MULTIPLY)
    ),
    STARRY_NIGHT(
        "Starry Night",
        Style.AssetStyle("starry_night.jpg", BlendMode.OVERLAY)
    ),
    FLOWERS(
        "Flowers",
        Style.AssetStyle("flowers.jpg", BlendMode.HARD_LIGHT)
    ),
    LION(
        "Lion",
        Style.AssetStyle("lion.jpg", BlendMode.OVERLAY)
    )
}

