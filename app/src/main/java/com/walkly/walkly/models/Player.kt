package com.walkly.walkly.models

import android.util.Log
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.*

// max of 3 stamina points
private const val MAX_STAMINA = 300

// update every 36 seconds (idk why 36)
private const val INTERVAL = 36000L

private const val TAG = "class Player"

data class Player(
    var name: String? = "2Lazy4u",
    var email: String? = "lazy@email.com",
    var currentEquipment: Equipment? = null,
    var currentHP: Long? = 0,
    var level: Long? = 1,
    var stamina: Long? = 300,
    var points: Long? = 0,
    var progress: Long? = 0,
    var lastUpdate: String? = "",
    var photoURL: String? = ""
) {
    // TODO: Add other collections list
    var id: String = ""
    var friendList: MutableList<Friend>? = null
    var equipmentList: MutableList<Equipment>? = null

    private var update = false
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    fun stopStaminaUpdates() {
        update = false
    }

    // Increases the stamina of the current player every 36 seconds
    fun startStaminaUpdates() {
        update = true
        scope.launch {
            while (update && stamina?.compareTo(MAX_STAMINA)!! < 0) {
                delay(INTERVAL)
                stamina = stamina?.inc()
                Log.d(TAG, "Current stamina: $stamina")
            }
        }
    }

    fun joinBattle() {
        stamina = stamina?.minus(100)
    }
}