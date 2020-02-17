package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.floor

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
    val stamina = MutableLiveData<Long>()
    val progress = MutableLiveData<Long>()

//    var stamina = 0L

    private var points = 0L

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)



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

        stamina.value = 0L
        progress.value = 0L

        val uid = auth.currentUser?.uid
        Log.d("uid", uid)
        userRef = firestore.collection("users").document(uid!!)

        userRef.addSnapshotListener { snapshot, exception ->

            if (exception != null){
                Log.e("Player", "listening exception", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {

                stamina.value = snapshot.data?.get("stamina") as Long
                level.value = snapshot.data?.get("level") as Long
                progress.value = snapshot.data?.get("progress") as Long
                points = snapshot.data?.get("points") as Long
                equipment.value = Equipment(snapshot.data?.get("equipped_weapon") as String)

            } else {
                Log.d("Player", "listener data is null")
            }

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


    suspend fun timeToStamin(){

        while (update && stamina.value?.compareTo(MAX_STAMINA) == -1){
            delay(INTERVAL)

            stamina.value = (stamina?.value)?.plus(1L)

        }
    }

    data class Rewardd (val image: String, val level: Int, val name: String, val type: String, val value: Int)

    // TODO: implement the reward interface

    fun getReward(enemyLevel: Int) {


        // increment players points
        Log.d(POINT_REWARDS_TAG, "old points = " + points.toString())
        val points = enemyLevel * ENEMY_LEVEL_POINTS
        Log.d(POINT_REWARDS_TAG, "new points = " + points.toString())
        //updatePoints(points)


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

        val type = arrayOf("equipments", "consumables").random()
        val reward = calculateReward(enemyLevel + r.toInt(), type)


        // save reward to user rewards in the database
        firestore.collection("users").document()
    }

    // gets Reward from the database, takes level as int, type is either consumables or equipments
    fun calculateReward(level: Int, type: String) : Rewardd {
        // initialize reference
        val itemsRef = firestore.collection(type)
        // create query to get the list of items that satisfies the condition
        val query = itemsRef.whereEqualTo("level", level)

        // gets a random item from the list
        val list = arrayOf(query)
        val item = list.random()
        // returns the item
        return item as Rewardd
    }



    // TODO: calculate progress form points
    // to save computation time calculate it and store it every time points change
    fun updatePoints(enemyLevel: Int){


        val level: Long
        val progress: Long
        val sumPoints = this.points + enemyLevel * ENEMY_LEVEL_POINTS

        level = floor(sumPoints / 150.0 + 1).toLong()
        progress = sumPoints - (level - 1) * 150


        userRef.update(
            hashMapOf(
                "level" to level,
                "progress" to progress,
                "points" to sumPoints
            ) as Map<String, Any>
        )


    }

    fun joinedBattle(){
        stamina.value = stamina.value?.minus(100L)
        syncModel()
    }
}