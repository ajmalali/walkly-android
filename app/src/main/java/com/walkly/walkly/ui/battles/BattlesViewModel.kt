package com.walkly.walkly.ui.battles

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldPath
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.walkly.walkly.models.Battle
import com.walkly.walkly.models.Enemy

private const val TAG = "BattlesViewModel"
class BattlesViewModel : ViewModel() {

    private val _battleList = MutableLiveData<List<Battle>>()
    val battleList: LiveData<List<Battle>>
        get() = _battleList

    private val _enemyList = MutableLiveData<List<Enemy>>()
    val enemyList: LiveData<List<Enemy>>
        get() = _enemyList

    private var tempBattleList = mutableListOf<Battle>()
    private var tempEnemyList = mutableListOf<Enemy>()


    private val battleIDs = mutableListOf<String>()
    private val enemyIDs = mutableListOf<String>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid


    init {
        getBattles()
        getEnemies()
    }

    fun getEnemies() {
        db.collection("enemies")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item = Enemy(document.data.get("level") as Long, document.id, document.data.get("health") as Long ,document.data.get("damage") as Long)
                    tempEnemyList.add(item)
                }
                tempEnemyList = tempEnemyList.toMutableList()
                _enemyList.value = tempEnemyList
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting friends documents.", exception)
            }
    }

    fun getBattles() {
        db.collection("online_battles")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    battleIDs.add(document.id)
                    Log.d(TAG, battleIDs.toString())
                }
                // create the list
                createBattleList(battleIDs)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting friends documents.", exception)
            }
    }
    private fun createBattleList(battleIDs: List<String>){
        val taskList = mutableListOf<Task<QuerySnapshot>>()
        for (idList in battleIDs.chunked(5)) {
            taskList.add(getBattlesFromIds(idList))
        }


        Tasks.whenAllSuccess<Task<QuerySnapshot>>(taskList)
            .addOnSuccessListener {
                tempBattleList = tempBattleList.toMutableList()
                _battleList.value = tempBattleList
                Log.d(TAG, "battle list created: $tempBattleList")
            }

    }

    private fun getBattlesFromIds(idList: List<String>): Task<QuerySnapshot> {
        return db.collection("online_battles")
            .whereIn(FieldPath.documentId(), idList)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item = Battle( document.data?.get("battle_name") as String,
                        (document.data?.get("players") as ArrayList<String>).size,
                        document.data?.get("host") as String ).addId(document.id)
                    tempBattleList.add(item)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting friend documents.", exception)
            }
    }

    fun joinListner(battleID: String){
        db.collection("online_battles").document(battleID).update("players", FieldValue.arrayUnion(userID))
    }
    fun hostListner(enemy_name: String, enemyHP: Int): String{
        var battle_id = ""
        db.collection("online_battles").add(
            hashMapOf("battle_state" to "on-going",
                "battle_name" to enemy_name,
                "host" to "rand_id",
                "players" to arrayListOf(userID),
                "combined_player_health" to 200,
                "enemy_health" to enemyHP
            )
        ).addOnSuccessListener { doc -> battle_id = doc.id
            }
       return battle_id
    }

}