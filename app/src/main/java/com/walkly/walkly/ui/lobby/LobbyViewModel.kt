package com.walkly.walkly.ui.lobby

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.tasks.await

private const val TAG = "LobbyViewModel"

class LobbyViewModel : ViewModel() {

    private val _playerList = MutableLiveData<List<BattlePlayer>>()
    val playerList: LiveData<List<BattlePlayer>>
        get() = _playerList

    private val _battleState = MutableLiveData<String>()
    val battleState: LiveData<String>
        get() = _battleState


    val currentPlayer = PlayerRepository.getPlayer()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val userID = FirebaseAuth.getInstance().currentUser?.uid
    private var battleID: String = ""
    var battle: OnlineBattle? = null

    private lateinit var playersRegistration: ListenerRegistration

    fun setupPlayersListener(id: String) {
        battleID = id
        var tempPlayerList: MutableList<BattlePlayer>
        playersRegistration = db.collection("online_battles")
            .document(battleID)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    battle = snapshot.toObject<OnlineBattle>()!!
                    tempPlayerList = battle?.players!!
                    _playerList.value = tempPlayerList
                    _battleState.value = battle?.battleState
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    fun invFriend(battleID: String) {
        Log.d(TAG, battleID)
        val players = hashMapOf(
            "players" to arrayListOf("h")
        )
        val invRef = db.collection("invites")
        val task = invRef.document(battleID).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val result = it.result
                if (result != null && result.exists()) {
                    invRef.document(battleID).update("players", FieldValue.arrayUnion("xx"))
                        .addOnSuccessListener { Log.d(TAG, "document successfully updated!") }
                        .addOnFailureListener { e -> Log.w(TAG, "trying to update but failed", e) }
                } else {
                    invRef.document(battleID).set(players)
                        .addOnSuccessListener { Log.d(TAG, "made a documebnt!") }
                        .addOnFailureListener { e -> Log.w(TAG, "trying to create a document", e) }
                }
            }
        }
    }

    suspend fun changeBattleState(state: String) {
        db.collection("online_battles").document(battleID)
            .update("battleState", state).await()
    }

    suspend fun changeBattlePublicity(status: String) {
        db.collection("online_battles").document(battleID)
            .update("type", status).await()
    }

    suspend fun changeEquipment(equipment: Equipment) {
        val players = _playerList.value!!
        for (player in players) {
            if (player.id == userID) {
                player.equipmentURL = equipment.image!!
                currentPlayer.currentEquipment = equipment
            }
        }

        db.collection("online_battles").document(battleID)
            .update("players", players).await()
    }

    fun removeListeners() {
        playersRegistration.remove()
    }
}