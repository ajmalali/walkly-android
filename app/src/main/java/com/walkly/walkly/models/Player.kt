package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

class Player (data: MutableLiveData<Long>) {

    // the difference between levels in points
    private val LEVEL_INCREMENT = 150
    // amount of points player gets for defeating enemy
    private val ENEMY_LEVEL_POINTS = 100L
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
    private val POINT_REWARDS_TAG = "points & reward"

    private val auth = FirebaseAuth.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance()
        .also {
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
                    Log.d(POINT_REWARDS_TAG, "init old points = " + points.toString())
                } catch (tce: TypeCastException) {
                    points = 0L
                    userRef.set(
                        hashMapOf(
                            "points" to 0L
                        ), SetOptions.merge()
                    )
                    Log.d(POINT_REWARDS_TAG, "not previous points")
                    Log.d(POINT_REWARDS_TAG, "init points set to " + points.toString())
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

    // TODO: implement the reward interface
    fun getReward(enemyLevel: Int) : Reward? {

        // increment players points
        Log.d(POINT_REWARDS_TAG, "old points = " + points.toString())
        points = enemyLevel * ENEMY_LEVEL_POINTS
        Log.d(POINT_REWARDS_TAG, "new points = " + points.toString())
        calculateProgress(points)

        val rand = java.util.Random()
        var r: Double

        r = rand.nextGaussian() * 0.7
        // rounding that keep it normally distributed
        if (r >= 0 )
            r = floor(r)
        else
            r = ceil(r)


        Log.d(POINT_REWARDS_TAG, "the random number is " + r.toString())


        // val reward = calculateReward(enemyLevel + r.toInt())

        // save reward to user rewards in the database

        return null
    }

    fun calculateReward(level: Int) : Reward{
        TODO()
        // get reward from the database
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

        var level  = 1L
        var progress = 0L
        userRef.get()
            .addOnSuccessListener {
                try {
                    level = it.data?.get("level") as Long
                    progress = it.data?.get("progress") as Long
                    Log.d(POINT_REWARDS_TAG, "current level is " + level.toString())
                    Log.d(POINT_REWARDS_TAG, "current progress is " + progress.toString())
                } catch (tce: TypeCastException) {
                    Log.d(POINT_REWARDS_TAG, "level & progress are not set")
                    level = 1L
                    progress= 0L

                    userRef.set(
                        hashMapOf(
                            "level" to level,
                            "progress" to progress
                        ), SetOptions.merge()
                    )
                }

                // 150 * n(n+1)/2

                // NOTE: progress is points - previous level * 150 it is not percentage
                if ((this.points + points) >= level * (level + 1) / 2 * LEVEL_INCREMENT){
                    progress = (this.points + points) - (level * (level + 1) / 2 * LEVEL_INCREMENT)
                    level++
                } else {
                    progress += points
                }



                userRef.update("level", level)
                userRef.update("progress", progress)

                Log.d(POINT_REWARDS_TAG, "saved level to " + level.toString())
                Log.d(POINT_REWARDS_TAG, "saved progress to " + progress.toString())

                userRef.update("points", (this.points + points))
                Log.d(POINT_REWARDS_TAG, "points updated to " + points.toString())
            }
    }
}