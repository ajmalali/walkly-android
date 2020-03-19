package com.walkly.walkly.offlineBattle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.repositories.ConsumablesRepository
import com.walkly.walkly.repositories.PlayerRepository
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.coroutines.*

private const val TAG = "OfflineBattleViewModel"

class OfflineBattleViewModel : ViewModel() {

    // used to specify how frequently enemy hits
    private val HIT_FREQUENCY = 3000L

    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    private val auth = FirebaseAuth.getInstance()
    private val currentPlayer = PlayerRepository.getPlayer()

    var enemy: Enemy? = null

    private var baseEnemyHP = -1L
    private var currentEnemyHp = 0L
    private var enemyHpPercentage = 100
    private var enemyDamage = 0L

    private var basePlayerHP = 1L
    private var currentPlayerHP = 0L
    private var playerHpPercentage: Int = 100
    private var playerDamage = 0L

    // view observes these
    // Health bar values
    val enemyHP = MutableLiveData<Int>()
    val playerHP = MutableLiveData<Int>()

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables

    private val _selectedConsumable = MutableLiveData<Consumable>()
    val selectedConsumable: LiveData<Consumable>
        get() = _selectedConsumable

    init {

        getConsumables()
        // get damage player can do based on equipment
//        playerDamage = currentPlayer.currentEquipment?.value!!
        playerDamage = 5L

        // get the starting player HP
        basePlayerHP = currentPlayer.level?.times(HP_MULTIPLAYER) ?: 1
        currentPlayerHP = basePlayerHP
        playerHpPercentage = ((currentPlayerHP * 100.0) / basePlayerHP).toInt()
        playerHP.value = playerHpPercentage
    }

    fun initEnemy(enemy: Enemy) {
        if (baseEnemyHP == -1L) {
            // get the starting enemy HP
//        baseEnemyHP = enemy.HP
            baseEnemyHP = 100L
            currentEnemyHp = baseEnemyHP
            enemyHP.value = currentEnemyHp.toInt()

            // get enemy damage
//            enemyDamage = enemy.damage
            enemyDamage = 1L
        }
    }


    // reduce enemy HP by distance walked * equipment value
    fun damageEnemy(steps: Float) {
//        currentEnemyHp -= steps * currentPlayer.currentEquipment?.value!!
        currentEnemyHp -= steps.toLong()
        enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toInt()
        enemyHP.value = enemyHpPercentage
        Log.d(TAG, "Current enemy health: $currentEnemyHp")
    }

    // WARNING: won't work while screen is off
    // reduce player HP by time * enemy damage
    suspend fun damagePlayer() {
        while (true) {
            delay(HIT_FREQUENCY)
            currentPlayerHP -= enemyDamage
            playerHpPercentage = ((currentPlayerHP * 100) / basePlayerHP).toInt()
            playerHP.value = playerHpPercentage
            Log.d(TAG, "Current player health = $currentPlayerHP")
        }
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