package com.walkly.walkly.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.repositories.PlayerRepository
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
        fetchEnemies(currentPlayer.level)
    }

    private fun fetchEnemies(playerLevel: Long?){
        CoroutineScope(IO).launch {
            _enemies.postValue(EnemyRepository.generateRandomEnemies(Player.level.value))
        }
    }
}