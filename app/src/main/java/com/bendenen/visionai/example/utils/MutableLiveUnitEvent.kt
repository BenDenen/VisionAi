package com.bendenen.visionai.example.utils

import androidx.lifecycle.MutableLiveData

/**
 * Convenient class for [MutableLiveEvent] with type [Unit].
 */
class MutableLiveUnitEvent : MutableLiveData<LiveEvent<Unit>>() {

    fun sendEvent() = postValue(LiveEvent(Unit))
}