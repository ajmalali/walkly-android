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
import kotlinx.android.synthetic.main.fragment_battle_activity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.Serializable
import java.util.*

private const val TAG = "OfflineBattleViewModel"

class OfflineBattleViewModel : ViewModel() {

    // used to specify how frequently enemy hits
    private val HIT_FREQUENCY = 3000L

    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    private val auth = FirebaseAuth.getInstance()
    private val currentPlayer = PlayerRepository.getPlayer()

    private var baseEnemyHP = -1L
    private var currentEnemyHp = 0L
    private var enemyHpPercentage = 100
    private var enemyDamage = 0L

    private var basePlayerHP = 1L
    private var currentPlayerHP = 0L
    private var playerHpPercentage = 100
    private var playerDamage = 0L

    // view observes these
    // Health bar values
    val enemyHP = MutableLiveData<Int>()
    val playerHP = MutableLiveData<Int>()

    private var scope = CoroutineScope(IO)

    init {
        // Get damage player can do based on equipment
//        playerDamage = currentPlayer.currentEquipment?.value!!
        playerDamage = 5L

        // Get the starting player HP
        basePlayerHP = currentPlayer.level?.times(HP_MULTIPLAYER) ?: 1
        currentPlayerHP = basePlayerHP
        playerHpPercentage = ((currentPlayerHP * 100.0) / basePlayerHP).toInt()
        playerHP.value = playerHpPercentage
    }

    fun initEnemy(enemy: Enemy) {
        if (baseEnemyHP == -1L) {
            // Get the starting enemy HP
//        baseEnemyHP = enemy.HP
            baseEnemyHP = 100L
            currentEnemyHp = baseEnemyHP
            enemyHP.value = currentEnemyHp.toInt()

            // Get enemy damage
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
        // reduce enemy HP by distance walked * equipment value
        walkedDistance.observe(activity, Observer {
            currentEnemyHp -= it * 5    // this should be `playerDamage` but it is always zero
            enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toLong()
            enemyHP.value = enemyHpPercentage
            Log.d(D_TAG, "distance = " + it)
        })

        // reduce player HP by time * enemy damage



    }

    class OfflineServiceInfo(val enemyHealth: Int, val enemyPower: Int,
                             val playerHealth: Int, val playerPower: Int, val frequency: Long) : Serializable
    private var pauseTime = -1L
    var battleEnded = false

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun startBackgroundService(){
        val extras = OfflineServiceInfo(
            currentEnemyHp.toInt(), enemyDamage.toInt(),
            currentPlayerHP.toInt(), 1, FREQUENCY)

        pauseTime = Date().time

        if (!battleEnded){
            Intent(activity, BackgroundOfflineBattleService::class.java).also {
                it.putExtra("info", extras)
                activity.startService(it)
            }

        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun StopBackgroundService(){
        Intent(activity, BackgroundOfflineBattleService::class.java).also {
            activity.stopService(it)
        }

        val damageMultiples = (Date().time - pauseTime) / FREQUENCY
        if (damageMultiples > 0){
            var playerHppercentage = (currentPlayerHP * 100) / basePlayerHP
            playerHP.value = playerHppercentage
        }

        scope.launch {
            val steps = distanceUtil.getStepsUntil(pauseTime)
            if (steps != null && steps != -1) {
                activity.tv_no_of_steps.text = "${steps + (activity as OfflineBattleActivity).steps}"
            }
        }
    }

    fun startBattle(){
        distanceUtil = DistanceUtil(activity, walkedDistance)

        scope.launch {
            damagePlayer()
        }
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