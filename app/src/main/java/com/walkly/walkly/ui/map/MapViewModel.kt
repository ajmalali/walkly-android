package com.walkly.walkly.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.repositories.PlayerRepository

class MapViewModel : ViewModel() {

    var currentPlayer = PlayerRepository.getPlayer()

    private val _stamina = MutableLiveData<Long>()
    val stamina: LiveData<Long?>
        get() = _stamina

    private val _level = MutableLiveData<Long>()
    val level: LiveData<Long?>
        get() = _level

    private val _progress = MutableLiveData<Long>()
    val progress: LiveData<Long?>
        get() = _progress

    init {
        _stamina.value = currentPlayer.stamina
        _level.value = currentPlayer.level
        _progress.value = currentPlayer.progress
    }
}