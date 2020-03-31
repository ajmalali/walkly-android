package com.walkly.walkly.ui.battles

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.repositories.PlayerRepository

private const val TAG = "BattlesViewModel"

class BattlesViewModel : ViewModel() {

    private var _battle = MutableLiveData<OnlineBattle>()
    val createBattle: LiveData<OnlineBattle>
        get() = _battle

    private var _selectedEnemy = MutableLiveData<Enemy>()
    val selectedEnemy: LiveData<Enemy>
        get() = _selectedEnemy

    private val _battleList = MutableLiveData<List<OnlineBattle>>()
    val onlineBattleList: LiveData<List<OnlineBattle>>
        get() = _battleList

    private val _enemyList = MutableLiveData<List<Enemy>>()
    val enemyList: LiveData<List<Enemy>>
        get() = _enemyList

    private var tempBattleList = mutableListOf<OnlineBattle>()
    private var tempEnemyList = mutableListOf<Enemy>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    init {
        getOnlineBattles()
    }

    fun getEnemies() {
        if (_enemyList.value == null) {
            db.collection("enemies") // TODO: change to online enemies
                .get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        val enemy = doc.toObject<Enemy>().addId(doc.id)
                        tempEnemyList.add(enemy)
                    }
                    Log.d(TAG, "Got enemies: ${tempEnemyList.size}")
                    _enemyList.value = tempEnemyList
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting enemy documents.", exception)
                }
        } else {
            _enemyList.value = tempEnemyList
        }
    }

    // Listening to online-battles in real time
    fun getOnlineBattles() {
        if (_battleList.value == null) {
            db.collection("online_battles")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        tempBattleList.clear()
                        for (document in value) {
                            val battle = document.toObject<OnlineBattle>()
                            tempBattleList.add(battle)
                        }
                        _battleList.value = tempBattleList
                    } else {
                        _battleList.value = emptyList()
                    }
                }
        } else {
            _battleList.value = tempBattleList
        }
    }

    fun joinListener(battleID: String) {
        db.collection("online_battles").document(battleID)
            .update("players", FieldValue.arrayUnion(userID))
    }

    // TODO: Change to OnlineEnemy
    fun selectEnemy(enemy: Enemy) {
        val battlesCollection = db.collection("online_battles")
        val battle = OnlineBattle(
            battleName = enemy.name,
            enemy = enemy,
            hostName = currentPlayer.name,
            enemyHealth = enemy.health
        )
        battlesCollection.add(
            battle
//            hashMapOf(
//                "battle_state" to "on-going",
//                "battle_name" to enemy_name,
//                "host" to "rand_id",
//                "players" to arrayListOf(userID),
//                "combined_player_health" to 200,
//                "enemy_health" to 300 //TODO this is hardcoded
//            )
        ).addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            battle.id = documentReference.id
            _battle.value = battle
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

}