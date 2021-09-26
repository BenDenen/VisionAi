package com.bendenen.visionai.example.screens.main.viewmodel

import androidx.lifecycle.ViewModel
import com.bendenen.visionai.example.screens.main.ui.MainScreenLayoutHandler
import com.bendenen.visionai.example.utils.MutableLiveUnitEvent

class MainViewModel : ViewModel() {

    val layoutHandler = object : MainScreenLayoutHandler {
        override fun onRequestStyleTransfer() {
            requestSegmentationEvent.sendEvent()
        }

        override fun onRequestSegmentation() {
            requestStyleTransferEvent.sendEvent()
        }

        override fun onRequestArCore() {
            requestArCoreEvent.sendEvent()
        }
    }

    val requestStyleTransferEvent = MutableLiveUnitEvent()
    val requestSegmentationEvent = MutableLiveUnitEvent()
    val requestArCoreEvent = MutableLiveUnitEvent()



}