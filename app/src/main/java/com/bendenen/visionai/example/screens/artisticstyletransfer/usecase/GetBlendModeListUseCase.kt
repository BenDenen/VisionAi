package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import com.bendenen.visionai.example.repository.BlendModeRepository
import com.bendenen.visionai.tflite.styletransfer.step.StyleTransferBlendMode

interface GetBlendModeListUseCase {

    suspend fun getBlendModeList(): List<StyleTransferBlendMode>

    class Impl(
        private val blendModeRepository: BlendModeRepository
    ) : GetBlendModeListUseCase {
        override suspend fun getBlendModeList(): List<StyleTransferBlendMode> =
            blendModeRepository.getBlendModeList()
    }
}