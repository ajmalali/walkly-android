package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class Player (data: MutableLiveData<Long>) {

    private lateinit var user: Map<String, Any>

    private var stamina = 0L

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val data = data
    private val INTERVAL = 36000L    // update every 36 seconds
    private val MAX_STAMINA = 300   // max of 3 stamina points

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        // TODO: get stamina from firebase
        val uid = auth.currentUser?.uid
        val userRef = firestore.collection("users").document(uid!!)
        userRef.get()
            .addOnSuccessListener {
                user = it.data!!
                Log.d("init data", user.toString())

                stamina = user["stamina"] as Long
            }
            .addOnFailureListener {
                Log.e("init","firestore read", it)
            }
    }


    // TODO: provide it to activities

    fun stopStaminaUpdates() {
        update = false
    }
    fun startStaminaUpdates() {
        update = true
        scope.launch {
            timeToStamin()
        }
    }

    // TODO: sync with firebase



    // TODO: boost stamina by distance

    // TODO: convert time to stamina

    suspend fun timeToStamin(){
        while (update && stamina < MAX_STAMINA){
            delay(INTERVAL)

            stamina++
            data.value = stamina
        }
    }


}