package com.walkly.walkly.offlineBattle

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.Player
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.coroutines.*
import java.time.temporal.TemporalAmount
import java.util.*

class OfflineBattleViewModel (activity: AppCompatActivity, enemy: Enemy) : ViewModel(){

    // used to specify how fact enemy hits
    private val FREQuNCY = 1000L
    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100
    // used to convert players steps to damage

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    val activity = activity

    val distanceUtil: DistanceUtil

    var enemyImage: String
    private val enemyDamage: Long
    private val baseEnemyHP: Long
    val enemyHP = MutableLiveData<Long>()
    private val playerDamage: Long
    private var basePlayerHP: Long = 0
    val playerHP = MutableLiveData<Long>()
    private val steps = MutableLiveData<Float>()

    init {
        enemyDamage = enemy.damage
        baseEnemyHP= enemy.HP
        enemyHP.value = baseEnemyHP
        enemyImage = enemy.image
        // uncomment after fixing player class design
        // basePlayerHP = Player().level * HP_MULTIPLAYER
        // playerHP.value = basePlayerHP
        playerDamage = Equipment().getUsedEquipment().value

        distanceUtil = DistanceUtil(activity, Calendar.getInstance().timeInMillis, 200L, steps)

        steps.observe(activity, androidx.lifecycle.Observer {
            enemyHP.value = baseEnemyHP - it.toInt() * playerDamage
        })

        scope.launch {
            damagePlayer(FREQuNCY, enemyDamage)
        }

    }


    fun onBattleCancelled(){
        TODO()
    }

    fun onActivityPause(){
        distanceUtil.stopUpdates()

    }

    fun onActivityResume(){
        distanceUtil.startUpdates()
    }

    // won't work while app is off
    suspend fun damagePlayer(freqncy: Long, amount: Long){
        while (basePlayerHP!! > 0){
            delay(freqncy)
            basePlayerHP -= amount
            playerHP.value = basePlayerHP
        }
    }
}