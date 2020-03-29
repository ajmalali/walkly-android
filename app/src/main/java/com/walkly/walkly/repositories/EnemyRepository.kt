package com.walkly.walkly.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.walkly.walkly.models.Enemy
import kotlinx.coroutines.tasks.await

private const val TAG = "EnemyRepository"

// Singleton repository object
object EnemyRepository {
    private val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .build()
    private val db = FirebaseFirestore.getInstance().also {
            it.firestoreSettings = settings
}
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val enemyCollection = db.collection("enemies")

    private var _enemy = MutableLiveData<Enemy>()
    val enemy: LiveData<Enemy>
        get() = _enemy

    suspend fun getEnemy(id: Int, playerLevel: Long?): Enemy {
        val enemydoc = enemyCollection.document(id.toString()).get().await()
                var enemy_ = Enemy(enemydoc.data?.get("name") as String,
                playerLevel!! + (1..3).random(),
                 enemydoc.id,
                 enemydoc.data?.get("image") as String,
                    playerLevel * 100 * (1..3).random(),
                    playerLevel * (1..3).random())
        return enemy_
    }

    suspend fun generateRandomEnemies(playerLevel: Long?): Array<Enemy>{
        var enemy1 = getEnemy(1, playerLevel)
        var enemy2 = getEnemy(2, playerLevel)
        var enemy3 = getEnemy(3, playerLevel)

        return arrayOf(enemy1, enemy2, enemy3)
    }
}