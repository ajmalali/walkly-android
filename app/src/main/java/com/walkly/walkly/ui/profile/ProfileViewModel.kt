package com.walkly.walkly.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.repositories.EquipmentRepository
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

private const val TAG = "ProfileViewModel"

class ProfileViewModel : ViewModel() {

    private val _equipments = MutableLiveData<List<Equipment>>()
    val equipments: LiveData<List<Equipment>>
        get() = _equipments

    private val _selectedEquipment = MutableLiveData<Equipment>()
    val selectedEquipment: LiveData<Equipment>
        get() = _selectedEquipment

    val currentPlayer = PlayerRepository.getPlayer()

    init {
        getEquipments()
    }

    private fun getEquipments() {
        CoroutineScope(IO).launch {
            _equipments.postValue(EquipmentRepository.getEquipments())
        }
    }

    fun selectEquipment(e: Equipment) {
        currentPlayer.currentEquipment = e
        _selectedEquipment.value = e
    }
}