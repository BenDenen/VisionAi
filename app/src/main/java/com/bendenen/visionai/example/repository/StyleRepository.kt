package com.bendenen.visionai.example.repository

import com.bendenen.visionai.tflite.styletransfer.step.Gallery
import com.bendenen.visionai.tflite.styletransfer.step.Style

interface StyleRepository {

    suspend fun getStyleList(): List<Style>

    suspend fun addStyle(style: Style)

    suspend fun addStyle(style: Style, position: Int)

    class Impl() : StyleRepository {

        private val styleList = Gallery.values().map {
            it.style
        }.toMutableList().also {
            it.add(0, Style.AssetStyle("", ""))
        }

        override suspend fun getStyleList(): List<Style> = styleList

        override suspend fun addStyle(style: Style) {
            styleList.add(style)
        }

        override suspend fun addStyle(style: Style, position: Int) {
            if (position == 0) {
                styleList.add(1, style)
            } else {
                styleList.add(position, style)
            }
        }
    }
}