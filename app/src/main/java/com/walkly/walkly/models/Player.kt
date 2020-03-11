package com.walkly.walkly.models

data class Player(
    var name: String? = "2Lazy4u",
    var email: String? = "lazy@email.com",
    var currentEquipment: Equipment? = null,
    var currentHP: Long? = 0,
    var level: Long? = 1,
    var stamina: Long? = 300,
    var points: Long? = 0,
    var progress: Long? = 0,
    var lastUpdate: String? = ""
) {
    var id: String = ""
    var friendList: MutableList<Friend>? = null
    var equipmentList: MutableList<Equipment>? = null
    var phoneNumber: String? = ""
    var photoURL: String? = ""

//    private var update = false
//    private val job = Job()
//    private val scope = CoroutineScope(Dispatchers.Main + job)

//
//    init {
//
//        stamina.value = 0L
//        progress.value = 0L

//    fun stopStaminaUpdates() {
//        update = false
//    }
//    fun startStaminaUpdates() {
//        update = true
//        scope.launch {
//            timeToStamin()
//        }
//    }
//
//
//
//    suspend fun timeToStamin(){
//
//        while (update && stamina.value?.compareTo(MAX_STAMINA) == -1) {
//            delay(INTERVAL)
//
//            stamina.value = (stamina?.value)?.plus(1L)
//
//        }
//    }
//
}