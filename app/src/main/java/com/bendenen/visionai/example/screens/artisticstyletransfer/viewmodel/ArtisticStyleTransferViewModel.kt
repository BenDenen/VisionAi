package com.bendenen.visionai.example.screens.artisticstyletransfer.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bendenen.visionai.VisionAi
import com.bendenen.visionai.example.screens.artisticstyletransfer.adapters.BlendModeAdapterCallback
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.ArtisticStyleTransferLayoutHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.ArtisticStyleTransferLayoutState
import com.bendenen.visionai.example.ui.content.ContentBlockHandler
import com.bendenen.visionai.example.screens.artisticstyletransfer.ui.styles.StylesBlockHandler
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
) : ViewModel(), BlendModeAdapterCallback, VisionAi.ResultListener {

    val state: ArtisticStyleTransferLayoutState = ArtisticStyleTransferLayoutState()
    val handler = ArtisticStyleTransferLayoutHandler(
        stylesBlockHandler = StylesBlockHandler(
            { style -> onStyleClick(style) },
            { onAddStyleClick() }
        ),
        contentBlockHandler = ContentBlockHandler { requestVideo() },
        processVideoAction = { startVideoProcessing() }
    )

    val requestVideoEvent = MutableLiveUnitEvent()
    val addNewStyleEvent = MutableLiveUnitEvent()

    fun initWithVideoPath(videoUri: Uri) {
        viewModelScope.launch {
            state.updateToVideoLoadingState()
            styleTransferFunctionsUseCase.initVisionAi(
                videoUri,
                "temp.mp4"
            )
            state.updateToVideoLoadedState(styleTransferFunctionsUseCase.getPreview(), getStyleListUseCase.getStyleList())
        }
    }

    private fun requestVideo() {
        requestVideoEvent.sendEvent()
    }

    private fun onStyleClick(style: Style) {
        viewModelScope.launch {
            state.updateToStyleProcessingState()
            styleTransferFunctionsUseCase.initStyle(style)
            state.updateToStyleProcessedState(styleTransferFunctionsUseCase.getPreview(), style)
        }
    }

    override fun onBlendModeClick(blendMode: StyleTransferBlendMode) {
        viewModelScope.launch {
            // TODO: Update blend Modes
        }
    }

    private fun onAddStyleClick() {
        addNewStyleEvent.sendEvent()
    }

    private fun startVideoProcessing() {
        styleTransferFunctionsUseCase.startVideoProcessing(this)
    }

    override fun onStepsResult(bitmap: Bitmap) {
//        state.layoutState = LayoutState.VideoProcessing(bitmap)
    }

    override fun onFileResult(filePath: String) {
        Log.e("MyTag", filePath)
//        state.layoutState.isLoading = false
        // TODO: Show play and share button
    }

    fun addStyle(style: Style) {
        viewModelScope.launch {
            state.updateToStyleProcessingState()
            addNewStyleUseCase.addNewStyle(style)
            state.updateToVideoLoadedState(styleTransferFunctionsUseCase.getPreview(), getStyleListUseCase.getStyleList())
        }
    }

    override fun onCleared() {
        styleTransferFunctionsUseCase.stopVideoProcessing()
        super.onCleared()
    }
}