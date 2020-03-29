package com.walkly.walkly.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

//private val settings = FirebaseFirestoreSettings.Builder()
//    .setPersistenceEnabled(true)
//    .build()
//private val firestore = FirebaseFirestore.getInstance().also {
//    it.firestoreSettings = settings
//}

//private var enemyCollection = firestore.collection("enemies")

data class Enemy(var name: String, var level: Long, var id: String, var image: String, var HP_: Long, var DMG_: Long)  {
//    val id = MutableLiveData<String>()
//    val name = MutableLiveData<String>()
//    val image = MutableLiveData<String>()
//    val HP = MutableLiveData<Long>()
//    val damage = MutableLiveData<Long>()
//    val level = MutableLiveData<Long>()


//    init{
//        val enemyRef = enemyCollection.document(id)
//        enemyRef.get()
//            .addOnSuccessListener {
//                this.id.value = id
//                name.value = it.data?.get("name") as String
//                image.value = it.data?.get("image") as String
//                HP.value = HP_
//                damage.value = DMG_
//                level.value = Level
//            }
//    }


//    companion object {
//        fun generateRandomEnemies(playerLevel: Long): Array<Enemy>{
//
//            var enemy1 = Enemy("",playerLevel + (1..3).random(), (1..3).random().toString(),"",playerLevel * 100 * (1..3).random(), playerLevel * (1..3).random())
//            var enemy2 = Enemy("",playerLevel + (1..3).random(), (1..3).random().toString(),"",playerLevel * 100 * (1..3).random(), playerLevel * (1..3).random())
//            var enemy3 = Enemy("",playerLevel + (1..3).random(), (1..3).random().toString(),"",playerLevel * 100 * (1..3).random(), playerLevel * (1..3).random())
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