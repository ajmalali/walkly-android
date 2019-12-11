package com.walkly.walkly.offlineBattle

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
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

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var player: Player
    private lateinit var distanceUtil: DistanceUtil
    private val walkedDistance = MutableLiveData<Float>()

    // BAD DESIGN: should get refactored
    private val stamina = MutableLiveData<Long>()
    private val auth = FirebaseAuth.getInstance()

    private var baseEnemyHP = 0L
    private var enemyDamage = 0L
    private var basePlayerHP = 0L
    private var playerDamage = 0L

    // view observes these
    val enemyImage = MutableLiveData<String>()
    val enemyHP = MutableLiveData<Long>()
    val playerHP = MutableLiveData<Long>()



    init {
        // BAD DESIGN: should get refactored
        if (auth.currentUser != null) {
            player = Player(stamina)
            // get damage player can do based on equipment
            player.equipment.observe(activity, androidx.lifecycle.Observer {
                it.value.observe(activity, androidx.lifecycle.Observer {value ->
                    playerDamage = value
                })
            })
        }
        // get the starting player HP
        player.level.observe(activity, androidx.lifecycle.Observer {
            basePlayerHP = it * HP_MULTIPLAYER
            playerHP.value = basePlayerHP
        })
        // get the starting enemy HP
        enemy.HP.observe(activity, androidx.lifecycle.Observer {
            baseEnemyHP = it
            enemyHP.value = baseEnemyHP
        })
        // get enemy image
        enemy.image.observe(activity, androidx.lifecycle.Observer {
            enemyImage.value = it
        })
        // get enemy damage
        enemy.damage.observe(activity, androidx.lifecycle.Observer {
            enemyDamage = it
        })

        distanceUtil = DistanceUtil(activity, Calendar.getInstance().timeInMillis, 500, walkedDistance)

        // reduce enemy HP by distance walked * equipment value
        walkedDistance.observe(activity, androidx.lifecycle.Observer {
            enemyHP.value = (baseEnemyHP - it * playerDamage).toLong()
        })

        // reduce player HP by time * enemy damage
        scope.launch {
            damagePlayer()
        }


    }

    // WARNING: won't work while screen is off
    suspend fun damagePlayer(){
        while (true){
            delay(FREQuNCY)
            basePlayerHP -= enemyDamage
            playerHP.value = basePlayerHP
        }
    }
}