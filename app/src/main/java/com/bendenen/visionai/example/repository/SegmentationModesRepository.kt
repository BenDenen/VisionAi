package com.bendenen.visionai.example.repository

import com.bendenen.visionai.tflite.bodysegmentation.step.SegmentationMode

interface SegmentationModesRepository {

    suspend fun getSegmentationModeList(): List<SegmentationMode>

    class Impl() : SegmentationModesRepository {

        override suspend fun getSegmentationModeList(): List<SegmentationMode> = SegmentationMode.values().toList()
    }
}