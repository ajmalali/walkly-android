package com.walkly.walkly.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Message

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val messages = mutableListOf<Message>()
    private var friendAvatar: String = ""

    fun getChat(friendId: String, callback: (List<Message>) -> Unit) {
        if (friendAvatar == ""){
            fetchFriendAvatar(friendId)
        }
        db.collection("chats")
            .whereEqualTo("from", friendId)
            .addSnapshotListener { snapshot, exception ->
                messages.clear()
                for (document in snapshot as QuerySnapshot){
                    messages.add(
                        document.toObject<Message>().addAvatar(friendAvatar)
                    )
                }

                messages.sort()
                callback(messages)
            }
    }

    private fun fetchFriendAvatar(friendId: String) {
        db.collection("users")
            .document(friendId)
            .get()
            .addOnSuccessListener {
                friendAvatar = it.data?.get("photoURL") as String
            }
    }
}
