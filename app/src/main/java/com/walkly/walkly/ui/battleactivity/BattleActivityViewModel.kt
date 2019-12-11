package com.walkly.walkly.ui.battleactivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.repositories.ConsumablesRepository

private const val TAG = "BattleActivityViewModel"

class BattleActivityViewModel : ViewModel() {

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables

    private val _selectedConsumable = MutableLiveData<Consumable>()
    val selectedConsumable: LiveData<Consumable>
        get() = _selectedConsumable

    fun getConsumables() {
        if (_consumables.value != null) {
            _consumables.value = ConsumablesRepository.consumableList
        } else {
            ConsumablesRepository.getConsumables { list ->
                _consumables.value = list
            }
        }
    }

    fun selectConsumable(consumable: Consumable) {
        _selectedConsumable.value = consumable
        Log.d(TAG, "$consumable")
        Log.d(TAG, "${_consumables.value}")
    }

    fun removeConsumable() {
        ConsumablesRepository.removeConsumable(selectedConsumable.value!!) {
            _consumables.value = it
        }
    }

}