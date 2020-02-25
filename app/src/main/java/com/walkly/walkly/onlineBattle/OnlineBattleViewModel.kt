package com.walkly.walkly.onlineBattle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.models.Player
import com.walkly.walkly.repositories.ConsumablesRepository
import kotlinx.coroutines.*
import java.util.*

private const val TAG = "OnlineBattleViewModel"

class OnlineBattleViewModel() : ViewModel() {

    private val FREQUENCY = 3000L
    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100

    val walkedDistance = MutableLiveData<Float>()

    // BAD DESIGN: should get refactored

    private val db = FirebaseFirestore.getInstance()

    var battleID: String = ""
    var baseEnemyHP = 0.0
    var enemyDamage = 5L // TODO: HARDCODED!
    var currentEnemyHp = 0.0
    var enemyHpPercentage = 100L
    var basePlayerHP = 1L
    var currentPlayerHP = 0L
    var playerDamage = 0L
    lateinit var docRef: DocumentReference
    lateinit var registration: ListenerRegistration

    // view observes these
    val enemyImage = MutableLiveData<String>()
    val enemyHP = MutableLiveData<Long>()
    val combinedHP = MutableLiveData<Long>()

    private val _consumables = MutableLiveData<List<Consumable>>()
    val consumables: LiveData<List<Consumable>>
        get() = _consumables

    private val _selectedConsumable = MutableLiveData<Consumable>()
    val selectedConsumable: LiveData<Consumable>
        get() = _selectedConsumable

    init {
        getConsumables()

        // TODO: HARDCODED VALUES! MUST BE REFACTORED
        basePlayerHP = 10L * HP_MULTIPLAYER
        currentPlayerHP = basePlayerHP
        combinedHP.value = (currentPlayerHP / basePlayerHP) * 100

        baseEnemyHP = 5000L.toDouble()
        currentEnemyHp = baseEnemyHP
        enemyHP.value = baseEnemyHP.toLong()

    }

    fun setUpListeners() {
        docRef = db.collection("online_battles").document(battleID)
        registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
//                combinedHP -= enemyDamage
//                playerHppercentage = (combinedHP * 100) / basePlayerHP
//                playerHP.value = playerHppercentage
//
                enemyHP.value = snapshot.data?.get("enemy_health") as Long
                combinedHP.value = snapshot.data?.get("combined_player_health") as Long
                Log.d(TAG, "Current data: ${snapshot.data}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    // WARNING: won't work while screen is off
    suspend fun damagePlayer() {
        var playerHppercentage = 100L

        while (combinedHP.value?.compareTo(0)!! >= 1) {
            delay(FREQUENCY)
            Log.d(TAG, "current player hp = $currentPlayerHP")

            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val newHealthValue = snapshot.getLong("combined_player_health")!! - enemyDamage
                transaction.update(docRef, "combined_player_health", newHealthValue)
                // Success
                null
            }.addOnSuccessListener { Log.d(TAG, "Transaction success!") }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Transaction failure.", e)
                }
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

    fun stopGame() {
        registration.remove()
    }

}