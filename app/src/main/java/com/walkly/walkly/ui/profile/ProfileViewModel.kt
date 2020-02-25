package com.walkly.walkly.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.repositories.EquipmentRepository

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _equipments = MutableLiveData<List<Equipment>>()
    val equipments: LiveData<List<Equipment>>
        get() = _equipments

    private val _selectedEquipment = MutableLiveData<Equipment>()
    val selectedEquipment: LiveData<Equipment>
        get() = _selectedEquipment

    init {
        getEquipments()
    }

    private fun getEquipments() {
        if (_equipments.value != null) {
            _equipments.value = EquipmentRepository.equipmentList
        } else {
            EquipmentRepository.getEquipment { list ->
                _equipments.value = list
            }
        }
    }

    fun selectEquipment(e:Equipment){
        EquipmentRepository.wearEquipment(e){equip ->
        _selectedEquipment.value = equip
        }
    }
}