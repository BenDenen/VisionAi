package com.bendenen.visionai.example.repository

import com.bendenen.visionai.tflite.styletransfer.step.StyleTransferBlendMode

interface BlendModeRepository {

    suspend fun getBlendModeList(): List<StyleTransferBlendMode>

    class Impl() : BlendModeRepository {
        override suspend fun getBlendModeList(): List<StyleTransferBlendMode> =
            StyleTransferBlendMode.values().toList()
    }
}