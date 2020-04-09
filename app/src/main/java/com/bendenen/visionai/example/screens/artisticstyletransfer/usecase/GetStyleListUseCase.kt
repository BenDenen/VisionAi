package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import android.content.Context
import com.bendenen.visionai.example.repository.StyleRepository
import com.bendenen.visionai.tflite.styletransfer.step.Style

interface GetStyleListUseCase {

    suspend fun getStyleList(): List<Style>

    class Impl(
        private val styleRepository: StyleRepository
    ) : GetStyleListUseCase {
        override suspend fun getStyleList(): List<Style> =
            styleRepository.getStyleList()
    }
}