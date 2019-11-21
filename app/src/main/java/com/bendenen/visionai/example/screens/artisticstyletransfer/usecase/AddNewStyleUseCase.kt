package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import com.bendenen.visionai.example.repository.StyleRepository
import com.bendenen.visionai.tflite.styletransfer.step.Style

interface AddNewStyleUseCase {

    suspend fun addNewStyle(style: Style)

    class Impl(
        private val styleRepository: StyleRepository
    ) : AddNewStyleUseCase {
        override suspend fun addNewStyle(style: Style) = styleRepository.addStyle(style, 1)
    }
}