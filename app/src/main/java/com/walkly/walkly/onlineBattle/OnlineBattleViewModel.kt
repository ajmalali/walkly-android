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
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.models.Shard
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

private const val TAG = "OnlineBattleViewModel"

class OnlineBattleViewModel : ViewModel() {

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

    private val _totalSteps = MutableLiveData<Long>()
    val totalSteps: LiveData<Long>
        get() = _totalSteps

    private var playerPosition = 0
    private var shardID = 0

    private var totalHealth = 0

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

                    // Calculate player position and combined base health
                    var health: Long = 0
                    for (i in tempPlayerList.indices) {
                        health += tempPlayerList[i].level * HP_MULTIPLAYER
                        if (tempPlayerList[i].id == currentPlayer.id) {
                            playerPosition = i
                        }
                    }
                    totalHealth = health.toInt()

                    // Update players health
                    currentPlayersHP = battle?.combinedPlayersHealth!!.toLong()
                    // New player joined
                    if (health > basePlayersHP) {
                        basePlayersHP = health
                    }
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
                    for (document in snapshot) {
                        val shard = document.toObject<Shard>()
                        steps += shard.steps
                    }

                    _totalSteps.value = steps.toLong()

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
        val offset = playerPosition * 4
        val docID = ((shardID++ % 4) + offset).toString()
        db.collection("online_battles")
            .document(battleID)
            .collection("enemy_damage_counter")
            .document("enemy_damage_doc")
            .collection("shards")
            .document(docID)
            .update("steps", FieldValue.increment(steps))
    }


    suspend fun damagePlayer() {
        while (currentPlayersHP >= 0) {
            delay(HIT_FREQUENCY)

            db.collection("online_battles").document(battleID)
                .update("combinedPlayersHealth", FieldValue.increment(-enemyDamage))
        }

    }

    // TODO: Make this faster for health
    fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> {
                damageEnemy(consumableValue.toLong())
            }
            "health" -> {
                if (currentPlayersHP + consumableValue <= basePlayersHP) {
                    db.collection("online_battles").document(battleID)
                        .update(
                            "combinedPlayersHealth",
                            FieldValue.increment(consumableValue.toLong())
                        )
                } else {
                    val battleRef = db.collection("online_battles").document(battleID)
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(battleRef)

                        var combinedPlayersHealth = snapshot.getLong("combinedPlayersHealth")!!
                        combinedPlayersHealth += consumableValue
                        if (combinedPlayersHealth > totalHealth) {
                            combinedPlayersHealth = totalHealth.toLong()
                        }

                        transaction.update(
                            battleRef,
                            "combinedPlayersHealth",
                            combinedPlayersHealth
                        )
                        null
                    }
                }

            }
        }
    }

    suspend fun removeCurrentPlayer() {
        stopGame()
        val players = _playerList.value!!.toMutableList()
        val newPlayers = players.filter { it.id != userID }

        Log.d(TAG, "Players: $newPlayers")

        db.collection("online_battles").document(battleID)
            .update(
                "players", newPlayers,
                "playerCount", FieldValue.increment(-1)
            ).await()

        if (newPlayers.isEmpty()) {
            changeBattleState("Finished")
        }
    }

    suspend fun changeBattleState(state: String) {
        db.collection("online_battles").document(battleID)
            .update("battleState", state).await()
    }

    fun stopGame() {
        // Locally stop everything
        battleRegistration.remove()
        enemyHealthRegistration.remove()
        currentPlayersHP = -1
        currentEnemyHp = -1
    }

}