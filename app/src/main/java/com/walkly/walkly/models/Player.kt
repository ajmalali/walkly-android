package com.walkly.walkly.models

import com.google.firebase.firestore.Exclude

private const val TAG = "class Player"

data class Player(
    var name: String? = "2Lazy4u",
    var email: String? = "lazy@email.com",
    var currentEquipment: Equipment? = null,
    var currentHP: Long? = 100L,
    var level: Long? = 1,
    var stamina: Long? = 300L, // TODO: Could be LiveData
    var points: Long? = 0,
    var progress: Long? = 0,
    var lastUpdate: String? = null,
    var photoURL: String? = null
) {

    @get:Exclude var id: String? = null

    fun joinBattle() {
        stamina = stamina?.minus(100)
    }
}