package com.walkly.walkly.ui.consumables

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.repositories.ConsumablesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "ConsumablesViewModel"

class ConsumablesViewModel: ViewModel() {

    private var scope = CoroutineScope(Dispatchers.IO)

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables

    private val _selectedConsumable = MutableLiveData<Consumable>()
    val selectedConsumable: LiveData<Consumable>
        get() = _selectedConsumable

    init {
        // Get current player's consumables
        getConsumables()
    }

    private fun getConsumables() {
        scope.launch {
            _consumables.postValue(ConsumablesRepository.getConsumables())
        }
    }

    fun selectConsumable(consumable: Consumable) {
        _selectedConsumable.value = consumable
        Log.d(TAG, "${_selectedConsumable.value}")
    }

    fun removeSelectedConsumable() {
        _consumables.value = ConsumablesRepository.removeConsumable(selectedConsumable.value!!)
    }
}