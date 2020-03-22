package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlin.math.floor

private const val TAG = "PlayerRepository"

// the difference between levels in points
private const val LEVEL_INCREMENT = 150
// amount of points player gets for defeating enemy
private const val ENEMY_LEVEL_POINTS = 100L
// max level difference between battle level and battle reward.
private const val LEVEL_DIFF = 3

object PlayerRepository {

    // Use this player object everywhere to access the current player by using the getPlayer() method
    private lateinit var currentPlayer: Player

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    private val scope = CoroutineScope(IO)

    fun getPlayer(): Player = runBlocking {
        if (::currentPlayer.isInitialized) {
            currentPlayer
        } else {
            initPlayer()
            currentPlayer
        }
    }

    // Gets the current player from firestore and initializes the currentPlayer object
    suspend fun initPlayer() {
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
        currentPlayer = snapshot.toObject<Player>()!!
        currentPlayer.apply {
            id = snapshot.id
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

    fun wearEquipment(equipment: Equipment) {
        currentPlayer.currentEquipment = equipment
    }

    // Syncs the current player with DB or store locally when no internet
    // TODO: Store locally when no internet
    // TODO: Update sub-collections also
    suspend fun syncPlayer() {
        userDocument.update(
            hashMapOf<String, Any?>(
            "name" to currentPlayer.name,
            "email" to currentPlayer.email,
            "stamina" to currentPlayer.stamina,
            "points" to currentPlayer.points,
            "level" to currentPlayer.level,
            "progress" to currentPlayer.progress,
            "currentEquipment" to currentPlayer.currentEquipment,
            "currentHP" to currentPlayer.currentHP,
            "lastUpdate" to null, // TODO: Still required?
            "photoURL" to currentPlayer.photoURL.toString()
        )).await()
    }
}