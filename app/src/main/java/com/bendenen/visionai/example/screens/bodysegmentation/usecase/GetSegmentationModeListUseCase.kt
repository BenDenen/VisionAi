package com.bendenen.visionai.example.screens.bodysegmentation.usecase

import com.bendenen.visionai.example.repository.SegmentationModesRepository
import com.bendenen.visionai.tflite.bodysegmentation.step.SegmentationMode

interface GetSegmentationModeListUseCase {

    suspend fun getSegmentationModeList(): List<SegmentationMode>

    class Impl(
        private val segmentationModesRepository: SegmentationModesRepository
    ) : GetSegmentationModeListUseCase {

        override suspend fun getSegmentationModeList(): List<SegmentationMode> =
            segmentationModesRepository.getSegmentationModeList()
    }
}