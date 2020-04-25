package com.walkly.walkly.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.walkly.walkly.models.Enemy
import kotlinx.coroutines.tasks.await

private const val TAG = "EnemyRepository"

// Singleton repository object
object EnemyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val enemyCollection = db.collection("enemies")

    private var _enemy = MutableLiveData<Enemy>()
    val enemy: LiveData<Enemy>
        get() = _enemy

    private suspend fun getEnemy(id: Int, playerLevel: Long): Enemy {
        val enemydoc = enemyCollection.document(id.toString()).get().await()
        val enemy_ = enemydoc.toObject<Enemy>()

        enemy_?.apply {
            level = playerLevel + (0..3).random()
            health = playerLevel * 100 * (1..3).random()
        }

        return enemy_!!
    }

    // Gets n random enemies
    suspend fun generateRandomEnemies(playerLevel: Long): List<Enemy> {
        val n = 3
        val enemies = enemyCollection.get().await().toObjects<Enemy>().toMutableList()

        for (enemy in enemies) {
            enemy.apply {
                level = playerLevel + (0..3).random()
                health = playerLevel * 100 * (1..3).random()
            }
        }

        enemies.shuffle()
        if (n <= enemies.size) {
            return enemies.subList(0, n)
        }

        return enemies.subList(0, enemies.size)
    }
}