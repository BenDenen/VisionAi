package com.bendenen.visionai.example.repository

import android.graphics.BlendMode

interface BlendModeRepository {

    suspend fun getBlendModeList(): List<BlendMode>

    class Impl() : BlendModeRepository {
        override suspend fun getBlendModeList(): List<BlendMode>  = BlendMode.values().toList()
    }

}