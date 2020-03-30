package com.walkly.walkly.onlineBattle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.walkly.walkly.models.Consumable
import com.walkly.walkly.repositories.ConsumablesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await

private const val TAG = "OnlineBattleViewModel"

class OnlineBattleViewModel() : ViewModel() {

    private val FREQUENCY = 3000L
    // used to convert player level to HP
    private val HP_MULTIPLAYER = 100


    // BAD DESIGN: should get refactored

    private val db = FirebaseFirestore.getInstance()

    var battleID: String = ""
    var baseEnemyHP = 0L
    var enemyDamage = 3L // TODO: HARDCODED!
    var currentEnemyHp = 0L
    var basePlayerHP = 0L
    var currentPlayerHP = 0L
    var enemyHpPercentage = 100L
//    var playerDamage = 0L

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
//        getConsumables()
        combinedHP.value = 100L
        enemyHP.value = 100L
        // TODO: HARDCODED VALUES! MUST BE REFACTORED
//        basePlayerHP = 10.0 * HP_MULTIPLAYER
//        currentPlayerHP = basePlayerHP
//        combinedHP.value = floor((currentPlayerHP / basePlayerHP) * 100).toLong()
//
//        baseEnemyHP = 5000L.toDouble()
//        currentEnemyHp = baseEnemyHP
//        enemyHP.value = baseEnemyHP.toLong()
    }

    suspend fun setUpListeners() {
        docRef = db.collection("online_battles").document(battleID)
        val result = docRef.get().await()
        baseEnemyHP = result.getLong("enemy_health")!!
        basePlayerHP = result.getLong("combined_player_health")!!

        if (baseEnemyHP < 100) {
            docRef.update("enemy_health", 200).await()
            baseEnemyHP = 200
        }

        if (basePlayerHP < 100) {
            docRef.update("combined_player_health", 200).await()
            basePlayerHP = 200
        }

        withContext(Main) {
            currentEnemyHp = baseEnemyHP
            currentPlayerHP = basePlayerHP
        }

        registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                currentEnemyHp = snapshot.getLong("enemy_health")!!
                enemyHP.value = ((currentEnemyHp * 100.0) / baseEnemyHP).toLong()

                currentPlayerHP = snapshot.getLong("combined_player_health")!!
                combinedHP.value = ((currentPlayerHP * 100.0) / basePlayerHP).toLong()
                Log.d(TAG, "Current data: ${snapshot.data}")
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    fun damageEnemy(steps: Float) {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val newHealthValue = snapshot.getLong("enemy_health")!! - steps
            transaction.update(docRef, "enemy_health", newHealthValue)
            // Success
            null
        }.addOnSuccessListener { Log.d(TAG, "Enemy Damaged") }
            .addOnFailureListener { e ->
                Log.w(TAG, "Transaction failure. (Enemy)", e)
            }
    }

    // WARNING: won't work while screen is off
    suspend fun damagePlayer() {
        while (currentPlayerHP >= 0) {
            delay(FREQUENCY)
            Log.d(TAG, "current player hp = $currentPlayerHP")

            docRef.update("combined_player_health", FieldValue.increment(-enemyDamage))
//            db.runTransaction { transaction ->
//                val snapshot = transaction.get(docRef)
//                val newHealthValue = snapshot.getLong("combined_player_health")!! - enemyDamage
//                transaction.update(docRef, "combined_player_health", newHealthValue)
//                // Success
//                null
//            }.addOnSuccessListener { Log.d(TAG, "Player damaged") }
//                .addOnFailureListener { e ->
//                    Log.w(TAG, "Transaction failure.", e)
//                }
        }

    }

//    private fun getConsumables() {
//        if (_consumables.value != null) {
//            _consumables.value = ConsumablesRepository.consumableList
//        } else {
//            ConsumablesRepository.getConsumables { list ->
//                _consumables.value = list
//            }
//        }
//    }
//
//    fun selectConsumable(consumable: Consumable) {
//        _selectedConsumable.value = consumable
//    }
//
//    fun removeSelectedConsumable() {
//        ConsumablesRepository.removeConsumable(selectedConsumable.value!!) { updatedList ->
//            _consumables.value = updatedList
//        }
//    }

    fun stopGame() {
        registration.remove()
        currentPlayerHP = -1
        currentEnemyHp = -1
    }

}