package com.walkly.walkly.models

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings


class Enemy(id: String) {

//    var name: String = ""
//    var HP: Long = 0
//    var damage: Long = 0
//    var level: Long = 0

    val name = MutableLiveData<String>()
    val image = MutableLiveData<String>()
    val HP = MutableLiveData<Long>()
    val damage = MutableLiveData<Long>()
    val level = MutableLiveData<Long>()

    private lateinit var location: Location

    private val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance().also {
        it.firestoreSettings = settings
    }

    init{
        val enemyRef = firestore.collection("enemies").document(id)
        enemyRef.get()
            .addOnSuccessListener {
                name.value = it.data?.get("name") as String
                image.value = it.data?.get("image") as String
                HP.value = it.data?.get("health") as Long
                damage.value = it.data?.get("damage") as Long
                level.value = it.data?.get("level") as Long
            }

    }

//    companion object {
//        fun generateRandomEnemies(l : Location): Array<Enemy>{
//            //get
//            var enemy1: Enemy = Enemy()
//            var enemy2: Enemy = Enemy()
//            var enemy3: Enemy = Enemy()
//
//            return arrayOf(enemy1, enemy2, enemy3)
//        }
//    }


//    fun setEnemyLocation(lat: Double, long: Double){
//        this.location.latitude= lat
//        this.location.longitude  = long
//    }
//
//    fun decreaseHP(damage: Double){
//        this.HP -= damage
//    }

}