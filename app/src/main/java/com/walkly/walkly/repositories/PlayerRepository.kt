package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.getField
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.Friend
import com.walkly.walkly.models.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.floor

private const val TAG = "PlayerRepository"

// the difference between levels in points
private const val LEVEL_INCREMENT = 150
// amount of points player gets for defeating enemy
private const val ENEMY_LEVEL_POINTS = 100L
// max level difference between battle level and battle reward.
private const val LEVEL_DIFF = 3
// update every 36 seconds
private const val INTERVAL = 36000L
// max of 3 stamina points
private const val MAX_STAMINA = 300

object PlayerRepository {

    private lateinit var currentPlayer: Player

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    private val scope = CoroutineScope(IO)

    init {

    }

    fun getPlayer(): Player {
        return currentPlayer
    }

    // Gets the current player from firestore and initializes the currentPlayer object
    suspend fun initCurrentPlayer() {
        try {
            val snapshot = userDocument.get().await()
            deserialize(snapshot)
            Log.d(TAG, "Player object initialized: $currentPlayer")
        } catch (e: FirebaseFirestoreException) {
            Log.d(TAG, "Error in getting player document: $e")
        } catch (e: Exception) {
            Log.d(TAG, "Some Error: $e")
        }
    }

    private fun deserialize(snapshot: DocumentSnapshot) {
        currentPlayer = snapshot.toObject(Player::class.java)!!
        currentPlayer.apply {
            id = snapshot.id
        }
    }

    // Get friends from friends sub-collection of the current player
    suspend fun getFriends() {
        val friends = userDocument.collection("friends").get().await()
        for (friend in friends.documents) {
            currentPlayer.friendList?.add(friend.toObject(Friend::class.java)!!)
        }
    }

    // Get equipment from equipment sub-collection of the current player
    suspend fun getEquipments() {
        val equipments = userDocument.collection("friends").get().await()
        for (equipment in equipments.documents) {
            currentPlayer.equipmentList?.add(equipment.toObject(Equipment::class.java)!!)
        }
    }

    // Updates local player object in real time
    private fun initListeners() {
        userDocument.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Player listen failed", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                currentPlayer = snapshot.toObject(Player::class.java)!!
            } else {
                Log.d(TAG, "listener data is null")
            }
        }
    }

    // TODO: calculate progress form points
    // to save computation time calculate it and store it every time points change
    fun updatePoints(enemyLevel: Int) {
        scope.launch {
            try {
                val level: Long
                val progress: Long
                val sumPoints = (currentPlayer.points ?: 0) + enemyLevel * ENEMY_LEVEL_POINTS

                level = floor(sumPoints / 150.0 + 1).toLong()
                progress = sumPoints - (level - 1) * 150

                userDocument.update(
                    hashMapOf(
                        "level" to level,
                        "progress" to progress,
                        "points" to sumPoints
                    ) as Map<String, Any>
                ).await()
            } catch (e: FirebaseFirestoreException) {
                Log.d(TAG, "Error in updating points")
            }
        }
    }

    fun joinedBattle() {
        currentPlayer.stamina = currentPlayer.stamina?.minus(100)
    }

    fun syncModel() {
        userDocument.update("stamina", currentPlayer.stamina)
    }
}