package com.walkly.walkly.pvp

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
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.models.Shard
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

private const val TAG = "PVPViewModel"

class PVPViewModel : ViewModel() {

    lateinit var battleID: String

    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    var baseHostHP = -1L
    private var currentHostHp = 0L
    private var hostHpPercentage = 100
    private var shardId: Int = 0

    var baseOpponentHP = 1L
    private var currentOpponentHP = 0L
    private var opponentHpPercentage = 100

    // Observables
    // Health bar values
    private val _hostHP = MutableLiveData<Int>()
    val hostHP: LiveData<Int>
        get() = _hostHP

    private val _opponentHP = MutableLiveData<Int>()
    val opponentHP: LiveData<Int>
        get() = _opponentHP

    private val _battle = MutableLiveData<PVPBattle>()
    val battle: LiveData<PVPBattle>
        get() = _battle

    private val _hostSteps = MutableLiveData<Long>()
    val hostSteps: LiveData<Long>
        get() = _hostSteps

    private val _opponentSteps = MutableLiveData<Long>()
    val opponentSteps: LiveData<Long>
        get() = _opponentSteps

    private lateinit var hostHealthListener: ListenerRegistration
    private lateinit var opponentHealthListener: ListenerRegistration
    private lateinit var battleListener: ListenerRegistration

    init {
        _hostHP.value = 100
        _opponentHP.value = 100
        _hostSteps.value = 0
        _opponentSteps.value = 0
    }

    fun setupHealthListeners(
        host: BattlePlayer?,
        opponent: BattlePlayer?
    ) {
        baseHostHP = host?.level?.times(HP_MULTIPLAYER)!!
        baseOpponentHP = opponent?.level?.times(HP_MULTIPLAYER)!!

        hostHealthListener = db.collection("pvp_battles").document(battleID)
            .collection("host_damage_counter")
            .document("host_damage_doc")
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

                    _opponentSteps.value = steps.toLong()

                    if (steps >= 0) {
                        // Update host health
                        currentHostHp = baseHostHP - steps
                        hostHpPercentage = ((currentHostHp * 100.0) / baseHostHP).toInt()
                        _hostHP.value = hostHpPercentage
                    }
                } else {
                    Log.d(TAG, "host data: null")
                }
            }

        opponentHealthListener = db.collection("pvp_battles").document(battleID)
            .collection("opponent_damage_counter")
            .document("opponent_damage_doc")
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

                    _hostSteps.value = steps.toLong()

                    if (steps >= 0) {
                        // Update opponent  health
                        currentOpponentHP = baseOpponentHP - steps
                        opponentHpPercentage =
                            ((currentOpponentHP * 100.0) / baseOpponentHP).toInt()
                        _opponentHP.value = opponentHpPercentage
                    }
                } else {
                    Log.d(TAG, "opponent data: null")
                }
            }

    }

    fun setupBattleListener() {
        battleListener = db.collection("pvp_battles").document(battleID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                _battle.value = snapshot?.toObject<PVPBattle>()
            }
    }

    fun damageOpponent(steps: Long) {
        val docID = (shardId++ % 4).toString()
        db.collection("pvp_battles")
            .document(battleID)
            .collection("opponent_damage_counter")
            .document("opponent_damage_doc")
            .collection("shards")
            .document(docID)
            .update("steps", FieldValue.increment(steps))
    }

    fun damageHost(steps: Long) {
        val docID = (shardId++ % 4).toString()
        db.collection("pvp_battles")
            .document(battleID)
            .collection("host_damage_counter")
            .document("host_damage_doc")
            .collection("shards")
            .document(docID)
            .update("steps", FieldValue.increment(steps))
    }

    fun useHostConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> damageOpponent(consumableValue.toLong())
            "health" -> {
                if ((currentHostHp + consumableValue) <= baseHostHP) {
                    damageHost(-consumableValue.toLong())
                } else {
                    val difference = (currentHostHp + consumableValue) - baseHostHP
                    damageHost(-(consumableValue - difference))
                }
            }
        }
    }

    fun useOpponentConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> damageHost(consumableValue.toLong())
            "health" -> {
                if ((currentOpponentHP + consumableValue) <= baseOpponentHP) {
                    damageOpponent(-consumableValue.toLong())
                } else {
                    val difference = (currentOpponentHP + consumableValue) - baseOpponentHP
                    damageOpponent(-(consumableValue - difference))
                }
            }
        }
    }

    fun stopGame() {
        // TODO: Delete battle
        hostHealthListener.remove()
        opponentHealthListener.remove()
        battleListener.remove()
    }

    suspend fun removeCurrentPlayer() {
        if (_battle.value?.host?.id == currentPlayer.id) {
            db.collection("pvp_battles").document(battleID).update("host", null).await()
        } else {
            db.collection("pvp_battles").document(battleID).update("opponent", null).await()
        }
    }
}