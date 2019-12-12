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

    init {
        getConsumables()
    }

    private fun getConsumables() {
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
    }

    fun removeSelectedConsumable() {
        ConsumablesRepository.removeConsumable(selectedConsumable.value!!) { updatedList ->
            _consumables.value = updatedList
        }
    }

}