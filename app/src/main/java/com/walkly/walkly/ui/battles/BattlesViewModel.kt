package com.walkly.walkly.ui.battles

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Invite
import com.walkly.walkly.models.PvP
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "BattlesViewModel"

class BattlesViewModel : ViewModel() {

    private var _battle = MutableLiveData<OnlineBattle>()
    val createBattle: LiveData<OnlineBattle>
        get() = _battle

    private var _pvpBattle = MutableLiveData<PvP>()
    val pvpBattle: LiveData<PvP>
        get() = _pvpBattle

    private val _battleList = MutableLiveData<List<OnlineBattle>>()
    val onlineBattleList: LiveData<List<OnlineBattle>>
        get() = _battleList

    private val _enemyList = MutableLiveData<List<Enemy>>()
    val enemyList: LiveData<List<Enemy>>
        get() = _enemyList

    private val _invitesList = MutableLiveData<List<Invite>>()
    val invitesList: LiveData<List<Invite>>
        get() = _invitesList

    private var tempBattleList = mutableListOf<OnlineBattle>()
    private var tempEnemyList = mutableListOf<Enemy>()
    private var tempInviteList = mutableListOf<Invite>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    init {
        getOnlineBattles()
        getEnemies()
        getInvites()
    }

    fun getInvites() {
        if (_invitesList.value == null) {
            db.collection("invites")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        tempInviteList.clear()
                        for (document in value) {
                            val invite = document.toObject<Invite>()
                            tempInviteList.add(invite)
                        }
                        Log.d(TAG, "Got em $tempInviteList")
                        _invitesList.value = tempInviteList
                    } else {
                        _invitesList.value = emptyList()
                    }
                }
        } else {
            _invitesList.value = tempInviteList
        }
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

    // TODO: FIX
    fun joinBattle(battleID: String) {
        currentPlayer.joinBattle()

//        db.collection("online_battles").document(battleID)
//            .update("players", FieldValue.arrayUnion(userID))
    }

    fun sendPvPInvite(): PvP {
        val document = db.collection("pvp_battles").document()
        val battle = PvP(
            id = document.id,
            hostName = currentPlayer.name,
            hostEquipmentImage = currentPlayer.currentEquipment?.image,
            hostImage = currentPlayer.photoURL
        )
        document.set(battle)

        db.collection("invites").add(
            hashMapOf(
                "type" to "pvp",
                "id" to document.id,
                "host" to currentPlayer.name
            )
        )

        return battle
    }

    fun joinPvPListener(id: String) {
        currentPlayer.joinBattle()
        CoroutineScope(IO).launch {
            db.collection("pvp_battles").document(id).set(
                hashMapOf(
                    "opponentName" to currentPlayer.name,
                    "opponentImage" to currentPlayer.photoURL,
                    "opponentEquipmentImage" to currentPlayer.currentEquipment?.image
                )
                , SetOptions.merge()
            ).await()

            db.collection("pvp_battles").document(id).get().addOnSuccessListener {
                _pvpBattle.value = it.toObject<PvP>()
            }
        }

        // TODO: Delete invite
//        db.collection("invites")
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
        battlesCollection.add(battle)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                battle.id = documentReference.id
                _battle.value = battle
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

}