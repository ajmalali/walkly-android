package com.walkly.walkly.pvp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.BattleInvite
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.tasks.await

class PVPInvitesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val currentPlayer = PlayerRepository.getPlayer()

    private val currentBattlePlayer = BattlePlayer(
        id = currentPlayer.id!!,
        name = currentPlayer.name!!,
        avatarURL = currentPlayer.photoURL!!,
        equipmentURL = currentPlayer.currentEquipment?.image!!,
        level = currentPlayer.level!!
    )

    private val _invitesList = MutableLiveData<List<BattleInvite>>()
    val invitesList: LiveData<List<BattleInvite>>
        get() = _invitesList

    private var tempInviteList = mutableListOf<BattleInvite>()

    init {
        getPVPInvites()
    }

    private fun getPVPInvites() {
        if (_invitesList.value == null) {
            db.collection("invites")
                .whereArrayContains("toIDs", currentPlayer.id!!)
                .whereEqualTo("type", "pvp")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        tempInviteList.clear()
                        for (document in value) {
                            val invite = document.toObject<BattleInvite>()
                            tempInviteList.add(invite)
                        }
                        _invitesList.value = tempInviteList
                    } else {
                        _invitesList.value = emptyList()
                    }
                }
        } else {
            _invitesList.value = tempInviteList
        }
    }

    suspend fun joinPVPBattle(id: String): PVPBattle? {
        currentPlayer.joinBattle()

        val battle = db.collection("pvp_battles").document(id).get()
            .await().toObject<PVPBattle>()

        battle?.opponent = currentBattlePlayer

        db.collection("pvp_battles").document(id).set(battle!!).await()
        db.collection("invites").document(id).delete()

        return battle
    }
}