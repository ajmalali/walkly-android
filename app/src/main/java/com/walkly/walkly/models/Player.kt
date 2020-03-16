package com.walkly.walkly.models

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

    fun joinBattle() {
        stamina = stamina?.minus(100)
    }
}