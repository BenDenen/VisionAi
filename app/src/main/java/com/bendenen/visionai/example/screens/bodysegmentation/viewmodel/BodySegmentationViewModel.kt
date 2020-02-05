package com.bendenen.visionai.example.screens.bodysegmentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bendenen.visionai.example.screens.bodysegmentation.usecase.BodySegmentationFunctionUseCase
import com.bendenen.visionai.example.screens.bodysegmentation.usecase.GetSegmentationModeListUseCase
import com.bendenen.visionai.example.utils.MutableLiveUnitEvent
import com.bendenen.visionai.tflite.bodysegmentation.step.SegmentationMode
import kotlinx.coroutines.launch

class BodySegmentationViewModel(
    val getSegmentationModeListUseCase: GetSegmentationModeListUseCase,
    private val bodySegmentationFunctionUseCase: BodySegmentationFunctionUseCase
) : ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    val isVideoLoaded = MutableLiveData<Boolean>()
    val previewImage = MutableLiveData<Bitmap>()
    val requestVideoEvent = MutableLiveUnitEvent()

    fun initWithVideoPath(videoUri: Uri) {
        viewModelScope.launch {
            isLoading.postValue(true)
            isVideoLoaded.postValue(false)
            bodySegmentationFunctionUseCase.initVisionAi(
                videoUri,
                "temp.mp4"
            )
            bodySegmentationFunctionUseCase.initWithModes(listOf(SegmentationMode.PERSON))
            previewImage.postValue(bodySegmentationFunctionUseCase.getPreview())

            isLoading.postValue(false)
            isVideoLoaded.postValue(true)
        }
    }

    fun requestVideo() {
        requestVideoEvent.sendEvent()
    }
}