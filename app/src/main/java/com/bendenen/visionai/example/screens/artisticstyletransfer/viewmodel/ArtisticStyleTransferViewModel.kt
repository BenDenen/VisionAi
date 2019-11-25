package com.bendenen.visionai.example.screens.artisticstyletransfer.viewmodel

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.BlendModeAdapterCallback
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.BlendModeItem
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.StyleAdapterCallback
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.AddNewStyleUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.ArtisticStyleTransferFunctionsUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.GetBlendModeListUseCase
import com.bendenen.visionai.example.screens.artisticstyletransfer.usecase.GetStyleListUseCase
import com.bendenen.visionai.example.utils.MutableLiveUnitEvent
import com.bendenen.visionai.tflite.styletransfer.step.Style
import com.bendenen.visionai.tflite.styletransfer.step.StyleTransferBlendMode
import kotlinx.coroutines.launch

class ArtisticStyleTransferViewModel(
    private val addNewStyleUseCase: AddNewStyleUseCase,
    private val getStyleListUseCase: GetStyleListUseCase,
    private val styleTransferFunctionsUseCase: ArtisticStyleTransferFunctionsUseCase,
    private val getBlendModeListUseCase: GetBlendModeListUseCase
) : ViewModel(),
    StyleAdapterCallback, BlendModeAdapterCallback, VisionAi.ResultListener {

    val isLoading = MutableLiveData<Boolean>()
    val isVideoLoaded = MutableLiveData<Boolean>()
    val isStyleSelected = MutableLiveData<Boolean>()
    val requestVideoEvent = MutableLiveUnitEvent()

    val previewImage = MutableLiveData<Bitmap>()

    val addNewStyleEvent = MutableLiveUnitEvent()

    val styleList = MutableLiveData<List<Style>>()
    val blendModeList = MutableLiveData<List<BlendModeItem>>()

    init {
        viewModelScope.launch {
            styleList.postValue(getStyleListUseCase.getStyleList())
            blendModeList.postValue(getBlendModeListUseCase.getBlendModeList().map {
                BlendModeItem(
                    it,
                    it.name
                )
            })
        }
    }

    fun initWithVideoPath(videoUri: Uri) {
        viewModelScope.launch {
            isLoading.postValue(true)
            isVideoLoaded.postValue(false)
            styleTransferFunctionsUseCase.initVisionAi(
                videoUri,
                "temp.mp4"
            )
            previewImage.postValue(styleTransferFunctionsUseCase.getPreview())

            isLoading.postValue(false)
            isVideoLoaded.postValue(true)
        }
    }

    fun requestVideo() {
        requestVideoEvent.sendEvent()
    }

    override fun onStyleClick(style: Style) {
        viewModelScope.launch {
            isStyleSelected.postValue(false)
            isLoading.postValue(true)
            styleTransferFunctionsUseCase.initStyle(style)
            previewImage.postValue(styleTransferFunctionsUseCase.getPreview())
            isLoading.postValue(false)
            isStyleSelected.postValue(true)
        }
    }

    override fun onBlendModeClick(blendMode: StyleTransferBlendMode) {
        viewModelScope.launch {
            isLoading.postValue(true)
            styleTransferFunctionsUseCase.setBlendMode(blendMode)
            previewImage.postValue(styleTransferFunctionsUseCase.getPreview())
            isLoading.postValue(false)
        }
    }

    override fun onAddStyleClick() {
        addNewStyleEvent.sendEvent()
    }

    override fun onStepsResult(bitmap: Bitmap) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFileResult(filePath: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun addStyle(style: Style) {
        viewModelScope.launch {
            addNewStyleUseCase.addNewStyle(style)
            val newList = getStyleListUseCase.getStyleList()
            styleList.postValue(newList)
        }
    }
}