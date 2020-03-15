package com.walkly.walkly.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.repositories.PlayerRepository

class MapViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }

    var currentPlayer = PlayerRepository.getPlayer()
    val text: LiveData<String> = _text
}