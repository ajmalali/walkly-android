package com.walkly.walkly.ui.lobby

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.tasks.await

private const val TAG = "PVPLobbyViewModel"

class PVPLobbyViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    private val _battle = MutableLiveData<PVPBattle>()
    val battle: LiveData<PVPBattle>
        get() = _battle

    private lateinit var battleRegistration: ListenerRegistration

    fun setupBattleListener(id: String) {
        Log.d(TAG, "Battle ID: $id")
        battleRegistration = db.collection("pvp_battles")
            .document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _battle.value = snapshot.toObject<PVPBattle>()
                }

            }
    }

    suspend fun changeEquipment(equipment: Equipment) {
        currentPlayer.currentEquipment = equipment
        if (_battle.value?.host?.id == userID) {
            val host = _battle.value?.host
            host?.equipmentURL = equipment.image!!
            db.collection("pvp_battles").document(_battle.value?.id!!)
                .update("host", host).await()
        } else {
            val opponent = _battle.value?.opponent
            opponent?.equipmentURL = equipment.image!!
            db.collection("pvp_battles").document(_battle.value?.id!!)
                .update("opponent", opponent).await()
        }


    }

    suspend fun changeBattleState(state: String) {
        db.collection("pvp_battles").document(_battle.value?.id!!)
            .update("status", state).await()
    }


}