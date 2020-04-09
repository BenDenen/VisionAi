package com.bendenen.visionai.example.screens.main.viewmodel

import androidx.lifecycle.ViewModel
import com.bendenen.visionai.example.utils.MutableLiveUnitEvent

class MainViewModel : ViewModel() {

    val requestStyleTransferEvent = MutableLiveUnitEvent()
    val requestSegmentationEvent = MutableLiveUnitEvent()

    fun requestStyleTransfer() = requestStyleTransferEvent.sendEvent()

    fun requestSegmentation() = requestSegmentationEvent.sendEvent()


}