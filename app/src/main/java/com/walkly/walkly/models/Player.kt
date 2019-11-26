package com.walkly.walkly.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*

class Player (data: MutableLiveData<Long>) {

    private var user: Map<String, Any>? = null
    private lateinit var userRef: DocumentReference

    private var stamina = 0L

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


}