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
import com.walkly.walkly.models.Shard
import com.walkly.walkly.repositories.PlayerRepository
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

    private var baseOpponentHP = 1L
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

    private lateinit var hostHealthListener: ListenerRegistration
    private lateinit var opponentHealthListener: ListenerRegistration

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

                    if (steps > 0) {
                        // Update host health
                        currentHostHp = baseHostHP - steps
                        hostHpPercentage = ((currentHostHp * 100.0) / baseHostHP).toInt()
                        _hostHP.value = hostHpPercentage
                    } else {
                        _hostHP.value = 100
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

                    if (steps > 0) {
                        // Update opponent  health
                        currentOpponentHP = baseOpponentHP - steps
                        opponentHpPercentage =
                            ((currentOpponentHP * 100.0) / baseOpponentHP).toInt()
                        _opponentHP.value = opponentHpPercentage
                    } else {
                        _opponentHP.value = 100
                    }

                } else {
                    Log.d(TAG, "host data: null")
                }
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
            "health" -> damageOpponent(-consumableValue.toLong())
        }
    }

    fun useOpponentConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> damageHost(consumableValue.toLong())
            "health" -> damageHost(-consumableValue.toLong())
        }
    }

    fun stopGame() {
        // TODO: Delete battle
        hostHealthListener.remove()
        opponentHealthListener.remove()
    }
}