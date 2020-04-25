package com.walkly.walkly.offlineBattle

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.repositories.EquipmentRepository
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await

import java.util.*

private const val TAG = "OfflineBattleViewModel"

class OfflineBattleViewModel : ViewModel() {

    var battleEnded: Boolean = false

    // used to specify how frequently enemy hits
    val HIT_FREQUENCY = 3000L

    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    val currentPlayer = PlayerRepository.getPlayer()

    var baseEnemyHP = -1L
    var currentEnemyHp = 0L
    var enemyHpPercentage = 100
    var enemyDamage = 0L

    var basePlayerHP = 1L
    var currentPlayerHP = 0L
    var playerHpPercentage = 100
    var playerDamage = 0L
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    // view observes these
    // Health bar values
    val enemyHP = MutableLiveData<Int>()
    val playerHP = MutableLiveData<Int>()

    var requiredSteps = 0
    var stepsTaken = 0
    var enemyLevel = 0L

    init {
        // Get damage player can do based on equipment
        playerDamage = currentPlayer.currentEquipment?.value!!

        // Get the starting player HP
        basePlayerHP = currentPlayer.level?.times(HP_MULTIPLAYER) ?: 1
        currentPlayerHP = basePlayerHP
        playerHpPercentage = ((currentPlayerHP * 100.0) / basePlayerHP).toInt()
        playerHP.value = playerHpPercentage
    }

    fun initEnemy(enemy: Enemy) {
        if (baseEnemyHP == -1L) {
            enemyLevel = enemy.level!!
            requiredSteps = (enemy.health!!).toInt()
            // Get the starting enemy HP
            baseEnemyHP = enemy.health!!
//            baseEnemyHP = 100L
            currentEnemyHp = baseEnemyHP
            enemyHP.value = currentEnemyHp.toInt()

            // Get enemy damage
            enemyDamage = enemy.damage!!
//            enemyDamage = 1L
        }
    }

    suspend fun getReward(): Equipment {
        val equipmentList = mutableListOf<Equipment>()
        val snapshot = db.collection("equipments").get().await()
        for (document in snapshot) {
            val equipment = document.toObject<Equipment>()
            equipmentList.add(equipment.addId(document.id))
        }

        val index = (0 until equipmentList.size).random()
        val reward = equipmentList[index]

        EquipmentRepository.addEquipment(reward)

        return reward
    }

    // reduce enemy HP by distance walked * equipment value
    fun damageEnemy(steps: Float) {
        stepsTaken += (steps * playerDamage).toInt()
        currentEnemyHp -= (steps * playerDamage).toLong()
//        currentEnemyHp -= steps.toLong()
        if (currentEnemyHp <= 0) {
            battleEnded = true
        }
        enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toInt()
        enemyHP.value = enemyHpPercentage
        Log.d(TAG, "Current enemy health: $currentEnemyHp")
    }


    // WARNING: won't work while screen is off
    // reduce player HP by time * enemy damage
    suspend fun damagePlayer() {
        while (currentPlayerHP >= 0 && !battleEnded) {
            delay(HIT_FREQUENCY)
            currentPlayerHP -= enemyDamage
            playerHpPercentage = ((currentPlayerHP * 100) / basePlayerHP).toInt()
            playerHP.value = playerHpPercentage
            Log.d(TAG, "Current player health = $currentPlayerHP")
        }

        battleEnded = true
    }

    fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> {
                currentEnemyHp -= consumableValue
                enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toInt()
                enemyHP.value = enemyHpPercentage
            }
            "health" -> {
                currentPlayerHP += consumableValue
                if (currentPlayerHP > basePlayerHP) {
                    currentPlayerHP = basePlayerHP
                }
                playerHpPercentage = ((currentPlayerHP * 100) / basePlayerHP).toInt()
                playerHP.value = playerHpPercentage
            }
        }
    }
}