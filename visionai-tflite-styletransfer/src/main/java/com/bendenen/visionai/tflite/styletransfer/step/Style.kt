package com.bendenen.visionai.tflite.styletransfer.step

import android.net.Uri

sealed class Style {

    data class AssetStyle(
        val styleFileName: String
    ) : Style()

    data class PhotoUriStyle(
        val styleFileUri: Uri
    ) : Style()
}

enum class Gallery(
    val styleName: String,
    val style: Style
) {
    PICASSO_SELF_PORTRAIT(
        "Picasso Self Portrait",
        Style.AssetStyle("picasso_self.jpg")
    ),
    SUNFLOWERS(
        "Sunflowers",
        Style.AssetStyle("sunflowers.jpg")
    ),
    HAPPY_MOOD_SPRING(
        "Happy Mood Spring",
        Style.AssetStyle("happy_mood_spring.jpg")
    ),
    STARRY_NIGHT(
        "Starry Night",
        Style.AssetStyle("starry_night.jpg")
    ),
    FLOWERS(
        "Flowers",
        Style.AssetStyle("flowers.jpg")
    ),
    LION(
        "Lion",
        Style.AssetStyle("lion.jpg")
    )
}

