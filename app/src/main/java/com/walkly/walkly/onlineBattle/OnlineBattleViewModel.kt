package com.walkly.walkly.onlineBattle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.models.Shard
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.*
import java.util.*

private const val TAG = "OnlineBattleViewModel"

class OnlineBattleViewModel() : ViewModel() {

    var battleEnded: Boolean = false
    var battleID: String = ""

    // used to specify how frequently enemy hits (3 seconds)
    val HIT_FREQUENCY = 3000L

    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()
    var battle: OnlineBattle? = null

    var baseEnemyHP = -1L
    private var currentEnemyHp = 0L
    private var enemyHpPercentage = 100
    var enemyDamage = 0L

    private var basePlayersHP = 1L
    private var currentPlayersHP = 0L
    private var playersHpPercentage = 100
    private var playerDamage = 0L

    // Observables
    // Health bar values
    private val _enemyHP = MutableLiveData<Int>()
    val enemyHP: LiveData<Int>
        get() = _enemyHP

    private val _combinedHP = MutableLiveData<Int>()
    val combinedHP: LiveData<Int>
        get() = _combinedHP

    private val _playerList = MutableLiveData<List<BattlePlayer>>()
    val playerList: LiveData<List<BattlePlayer>>
        get() = _playerList

    private var playerPosition = 0

    private lateinit var battleRegistration: ListenerRegistration
    private lateinit var enemyHealthRegistration: ListenerRegistration

    init {
        _combinedHP.value = 100
        _enemyHP.value = 100

        playerDamage = currentPlayer.currentEquipment?.value!!
    }

    fun setupBattleListener() {
        var tempPlayerList: MutableList<BattlePlayer>
        battleRegistration = db.collection("online_battles")
            .document(battleID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    battle = snapshot.toObject<OnlineBattle>()

                    // Update players
                    tempPlayerList = battle?.players!!
                    _playerList.value = tempPlayerList

                    // Update player position
                    for (i in tempPlayerList.indices) {
                        if (tempPlayerList[i].id == currentPlayer.id) {
                            playerPosition = i
                            break
                        }
                    }

                    currentPlayersHP = battle?.combinedPlayersHealth!!.toLong()

                    // Update players health
                    val newBaseHP = (battle?.playerCount?.times(HP_MULTIPLAYER))!!.toLong()
                    if (newBaseHP > basePlayersHP) {
                        // New player joined
                        currentPlayersHP += HP_MULTIPLAYER
                    } else if (newBaseHP < basePlayersHP) {
                        // Player left
//                        currentPlayersHP -= HP_MULTIPLAYER
                    }
                    basePlayersHP = newBaseHP
                    playersHpPercentage = ((currentPlayersHP * 100.0) / basePlayersHP).toInt()
                    _combinedHP.value = playersHpPercentage
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    fun setupEnemyHealthListener() {
        enemyHealthRegistration = db.collection("online_battles")
            .document(battleID)
            .collection("enemy_damage_counter")
            .document("enemy_damage_doc")
            .collection("shards")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                // Get total number of steps
                var steps = 0
                if (snapshot != null) {
//                    val list = snapshot.toObjects<Shard>()
//                    list.forEach { steps += it.steps }
                    for (document in snapshot) {
                        val shard = document.toObject<Shard>()
                        steps += shard.steps
                    }

                    // Update enemy health
                    currentEnemyHp = baseEnemyHP - steps
                    enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toInt()
                    _enemyHP.value = enemyHpPercentage
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    // Sets the steps value in the shard of the player position.
    fun damageEnemy(steps: Long) {
        db.collection("online_battles")
            .document(battleID)
            .collection("enemy_damage_counter")
            .document("enemy_damage_doc")
            .collection("shards")
            .document(playerPosition.toString())
            .update("steps", FieldValue.increment(steps))
    }


    suspend fun damagePlayer() {
        while (currentPlayersHP >= 0) {
            delay(HIT_FREQUENCY)
            Log.d(TAG, "current players hp = $currentPlayersHP")

            db.collection("online_battles").document(battleID)
                .update("combinedPlayersHealth", FieldValue.increment(-enemyDamage))
        }

    }

    fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> {
                damageEnemy(consumableValue.toLong())
            }
            "health" -> {
                db.collection("online_battles").document(battleID)
                    .update("combinedPlayersHealth", FieldValue.increment(consumableValue.toLong()))
            }
        }
    }

    fun stopGame() {
        // Delete battle
        battleRegistration.remove()
        enemyHealthRegistration.remove()
        currentPlayersHP = -1
        currentEnemyHp = -1
    }

}