package com.walkly.walkly.ui.lobby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

private val _playerList = MutableLiveData<List<String>>()
val playerList: LiveData<List<String>>
    get() = _playerList

class LobbyViewModel() : ViewModel() {

}