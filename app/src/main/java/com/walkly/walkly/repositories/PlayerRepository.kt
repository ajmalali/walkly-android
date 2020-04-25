package com.walkly.walkly.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlin.math.sqrt

private const val TAG = "PlayerRepository"

// the difference between levels in points
private const val LEVEL_INCREMENT = 150

// amount of points player gets for defeating enemy
private const val ENEMY_LEVEL_POINTS = 10L

// max level difference between battle level and battle reward.
private const val LEVEL_DIFF = 3

object PlayerRepository {
    // Use this player object everywhere to access the current player by using the getPlayer() method
    private lateinit var currentPlayer: Player

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    private val scope = CoroutineScope(IO)

    private val _level = MutableLiveData<Long>()
    val level: LiveData<Long>
        get() = _level

    private val _progress = MutableLiveData<Long>()
    val progress: LiveData<Long>
        get() = _progress

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

        _level.value = currentPlayer.level
        _progress.value = currentPlayer.progress
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
            )
        ).await()
    }

    /*
  Level 1 @ 0 points
  Level 2 @ 50 points
  Level 3 @ 150 points
  Level 4 @ 300 points
  Level 5 @ 500 points etc.
   */
    fun updatePoints(enemyLevel: Long) {
        val gainedPoints = enemyLevel * ENEMY_LEVEL_POINTS
        currentPlayer.apply {
            points = points?.plus(gainedPoints)
            val requiredPoints = 25 * (level!! + 1) * (level!! + 1) - 25 * (level!! + 1)
            level = (floor(25 + sqrt(625 + 100.0 * points!!)) / 50).toLong()
            _level.value = level
            progress = ((points!! * 100.0) / requiredPoints).toLong()
            _progress.value = progress
        }
    }
}