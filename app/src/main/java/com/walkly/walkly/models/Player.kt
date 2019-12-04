package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlin.random.Random

class Player (data: MutableLiveData<Long>) {

    // the difference between levels in points
    private val LEVEL_INCREMENT = 150
    // amount of points player gets for defeating enemy
    private val ENEMY_LEVEL_POINTS = 100
    // max level difference between battle level and battle reward.
    private val LEVEL_DIFF = 3
    private var user: Map<String, Any>? = null
    private lateinit var userRef: DocumentReference

    private var stamina = 0L

    private var points = 0L

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val data = data
    private val INTERVAL = 36000L    // update every 36 seconds
    private val MAX_STAMINA = 300   // max of 3 stamina points

    private val auth = FirebaseAuth.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance().also {
        // caches the data locally to work offline
        it.firestoreSettings = settings
    }

    init {
        val uid = auth.currentUser?.uid
        Log.d("uid", uid)
        userRef = firestore.collection("users").document(uid!!)
        userRef.get()
            .addOnSuccessListener {
                user = it.data
                Log.d("init data", user.toString())

                // try read users stamina from firestore
                try {
                    stamina = user?.get("stamina") as Long
                } catch (tce: kotlin.TypeCastException) {
                    // does not have stamina
                    // set it to 0
                    stamina = 0L
                    // create new field for stamina
                    userRef.set(
                        hashMapOf(
                            "stamina" to 0L
                        ), SetOptions.merge()
                    )
                }

                // try to read points
                try{
                    points = user?.get("points") as Long
                } catch (tce: TypeCastException) {
                    points = 0L
                    userRef.set(
                        hashMapOf(
                            "points" to 0L
                        ), SetOptions.merge()
                    )
                }

            }
            .addOnFailureListener {
                Log.e("init","firestore read", it)
            }
    }

    fun stopStaminaUpdates() {
        update = false
    }
    fun startStaminaUpdates() {
        update = true
        scope.launch {
            timeToStamin()
        }
    }

    fun syncModel(){
        userRef.update("stamina", stamina)
    }

    // TODO: boost stamina by distance

    suspend fun timeToStamin(){
        while (update && stamina < MAX_STAMINA){
            delay(INTERVAL)

            stamina++
            data.value = stamina
        }
    }

    data class Rewardd (val image: String, val level: Int, val name: String, val type: String, val value: Int)

    // TODO: implement the reward interface
    fun getReward(enemyLevel: Int) : Rewardd {

        // increment players points
        points += enemyLevel * ENEMY_LEVEL_POINTS
        calculateProgress(points)

        val r = java.util.Random().nextGaussian() * LEVEL_DIFF
        var type = arrayOf("equipments", "consumables").random()
        val reward = calculateReward(enemyLevel + r.toInt(), type)

        // save reward to user rewards in the database

        return reward
    }

    // gets Reward from the database, takes level as int, type is either consumables or equipments
    fun calculateReward(level: Int, type: String) : Rewardd{
        // initialize reference
        val itemsRef = firestore.collection(type)
        // create query to get the list of items that satisfies the condition
        val query = itemsRef.whereEqualTo("level", level).get()
        // gets a random item from the list
        val list = arrayOf(query)
        val item = list.random() as Rewardd
        // returns the item
        return item
    }

    // TODO: returns the level and the current points in the level to map
    data class Progress(val level: Long, val progress: Long)
    fun getProgress() : Progress{
        var level = 1L
        var progress = 0L
        userRef.get()
            .addOnSuccessListener {
                if (it.data?.get("points") == 0)
                    return@addOnSuccessListener
                else {
                    level = it.data?.get("level") as Long
                    progress = it.data?.get("progress") as Long
                }
            }
        return Progress(level, progress)

    }

    // TODO: calculate progress form points
    // to save computation time calculate it and store it every time points change
    fun calculateProgress(points: Long){
        userRef.update("points", points)
        var level  = 1L
        var progress = 0L
        userRef.get()
            .addOnSuccessListener {
                try {
                    level = it.data?.get("level") as Long
                    progress = it.data?.get("progress") as Long
                } catch (tce: TypeCastException) {
                    level = 0L
                    progress= 0L
                }
                if (progress + points >= level * level * LEVEL_INCREMENT){
                    level++
                    progress = progress + points - level * LEVEL_INCREMENT
                } else {
                    progress += points
                }

                userRef.set(
                    hashMapOf(
                        "level" to level,
                        "progress" to progress
                    ), SetOptions.merge()
                )
            }
    }
}