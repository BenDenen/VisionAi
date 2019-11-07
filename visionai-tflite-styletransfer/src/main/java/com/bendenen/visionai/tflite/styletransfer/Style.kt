package com.bendenen.visionai.tflite.styletransfer

sealed class Style {

    data class AssetStyle(
        val styleFileName: String
    ) : Style()
}

enum class Gallery(
    val style: Style
) {
    PICASSO_SELF_PORTRAIT(Style.AssetStyle("picasso_self.jpg")),
    SUNFLOWERS(Style.AssetStyle("sunflowers.jpg")),
    HAPPY_MOOD_SPRING(Style.AssetStyle("happy_mood_spring.jpg")),
    STARRY_NIGHT(Style.AssetStyle("starry_night.jpg")),
    FLOWERS(Style.AssetStyle("flowers.jpg"))
}

