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

object Player  {

    // the difference between levels in points
    private val LEVEL_INCREMENT = 150
    // amount of points player gets for defeating enemy
    private val ENEMY_LEVEL_POINTS = 100L
    // max level difference between battle level and battle reward.
    private val LEVEL_DIFF = 3
    private var user: Map<String, Any>? = null
    var userRef: DocumentReference
    val equipment = MutableLiveData<Equipment>()
    val level = MutableLiveData<Long>()

//    var stamina = 0L

    private var points = 0L

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    val  stamina: MutableLiveData<Long>

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

        stamina = MutableLiveData()
        stamina.value = 0L

        val uid = auth.currentUser?.uid
        Log.d("uid", uid)
        userRef = firestore.collection("users").document(uid!!)
        userRef.get()
            .addOnSuccessListener {
                user = it.data
                Log.d("init data", user.toString())

                // try read users stamina from firestore
                try {
                    stamina.value = user?.get("stamina") as Long
                } catch (tce: kotlin.TypeCastException) {
                    // does not have stamina
                    // set it to 0
                    stamina.value = 0L
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

                // try to read level
                try {
                    level.value = user?.get("level") as Long
                } catch (tce : TypeCastException) {
                    level.value = 1L
                    userRef.set(
                        hashMapOf(
                            "level" to 1L
                        ), SetOptions.merge()
                    )
                }

//                val equipmentId = user?.get("equipment") as String
//                equipment.value = Equipment(equipmentId)


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
        userRef.update("stamina", stamina.value)
    }

    // TODO: boost stamina by distance

    suspend fun timeToStamin(){

        while (update && stamina.value?.compareTo(MAX_STAMINA) == -1){
            delay(INTERVAL)

            stamina.value = (stamina?.value)?.plus(1L)

        }
    }

    data class Rewardd (val image: String, val level: Int, val name: String, val type: String, val value: Int)

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

        var type = arrayOf("equipments", "consumables").random()
        val reward = calculateReward(enemyLevel + r.toInt(), type)


        // save reward to user rewards in the database

        return null
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