package com.walkly.walkly.ui.lobby

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
val TAG = "LOBBYVM"
private val _playerList = MutableLiveData<List<String>>()
val playerList: LiveData<List<String>>
    get() = _playerList

private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
private val userID = FirebaseAuth.getInstance().currentUser?.uid

class LobbyViewModel() : ViewModel() {

    fun invFriend(battleID: String){
        Log.d(TAG, battleID)
        val players =  hashMapOf(
            "players" to arrayListOf("h")
        )
        val invRef = db.collection("invites")

        if(invRef.document(battleID) == null){
            invRef.document(battleID).update("players", FieldValue.arrayUnion("xx")).addOnSuccessListener { Log.d(TAG, "document successfully updated!") }
                .addOnFailureListener { e -> Log.w(TAG, "trying to update but failed", e) }
        } else {
            invRef.document(battleID).set(players).addOnSuccessListener { Log.d(TAG, "made a documebnt!") }
                .addOnFailureListener { e -> Log.w(TAG, "trying to create a document", e) }
        }


    }
}