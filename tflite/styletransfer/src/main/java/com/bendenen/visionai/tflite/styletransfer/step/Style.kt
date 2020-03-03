package com.bendenen.visionai.tflite.styletransfer.step

import android.net.Uri

sealed class Style {

    abstract val name: String

    data class AssetStyle(
        val styleFileName: String,
        override val name: String
    ) : Style()

    data class PhotoUriStyle(
        val styleFileUri: Uri,
        override val name: String
    ) : Style()
}

enum class Gallery(
    val style: Style
) {
    PICASSO_SELF_PORTRAIT(
        Style.AssetStyle("picasso_self.jpg", "Picasso Self Portrait")
    ),
    SUNFLOWERS(
        Style.AssetStyle("sunflowers.jpg", "Sunflowers")
    ),
    HAPPY_MOOD_SPRING(
        Style.AssetStyle("happy_mood_spring.jpg", "Happy Mood Spring")
    ),
    STARRY_NIGHT(
        Style.AssetStyle("starry_night.jpg", "Starry Night")
    ),
    FLOWERS(
        Style.AssetStyle("flowers.jpg", "Flowers")
    ),
    LION(
        Style.AssetStyle("lion.jpg", "Lion")
    )
}

