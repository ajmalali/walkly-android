package com.walkly.walkly.ui.battleactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.repositories.ConsumablesRepository

private const val TAG = "BattleActivityViewModel"

class BattleActivityViewModel: ViewModel() {

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables


    private fun getConsumables() {
        if (_consumables.value != null) {
            return
        }

        ConsumablesRepository.getConsumables { list ->
            _consumables.value = list
        }
    }

}