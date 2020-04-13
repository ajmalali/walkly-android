package com.walkly.walkly.ui.lobby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.walkly.walkly.models.PVPBattle
import com.walkly.walkly.repositories.PlayerRepository

class PVPLobbyViewModel: ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    val currentPlayer = PlayerRepository.getPlayer()

    private val _battleState = MutableLiveData<String>()
    val battleState: LiveData<String>
        get() = _battleState

    private val _battle = MutableLiveData<PVPBattle>()
    val battle: LiveData<PVPBattle>
        get() = _battle

    private lateinit var battleRegistration: ListenerRegistration

    fun setupBattleListener(id: String) {

    }

}