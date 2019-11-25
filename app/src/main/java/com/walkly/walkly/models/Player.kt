package com.walkly.walkly.models

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class Player (data: MutableLiveData<Int>) {

    private var stamina = 0

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val data = data
    private val INTERVAL = 36000L    // update every 36 seconds
    private val MAX_STAMINA = 300   // max of 3 stamina points

    init {
        // TODO: get stamina from firebase
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

            stamina++
            data.value = stamina
            delay(INTERVAL)
        }
    }


}