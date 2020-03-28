package com.walkly.walkly.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Player
import com.walkly.walkly.repositories.EnemyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private var _enemies = MutableLiveData<Array<Enemy>>()
    val enemies: LiveData<Array<Enemy>>
        get() = _enemies

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private fun fetchEnemies(playerLevel: Long?){
        CoroutineScope(IO).launch {
            _enemies.postValue(EnemyRepository.generateRandomEnemies(Player.level.value))
        }
    }

    init {
        fetchEnemies(Player.level.value)
    }

}