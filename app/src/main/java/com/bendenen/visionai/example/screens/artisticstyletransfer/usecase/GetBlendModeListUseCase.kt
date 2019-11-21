package com.bendenen.visionai.example.screens.artisticstyletransfer.usecase

import android.graphics.BlendMode
import com.bendenen.visionai.example.repository.BlendModeRepository

interface GetBlendModeListUseCase {

    suspend fun getBlendModeList() : List<BlendMode>

    class Impl(
        private val blendModeRepository: BlendModeRepository
    ):GetBlendModeListUseCase {
        override suspend fun getBlendModeList(): List<BlendMode> = blendModeRepository.getBlendModeList()
    }
}