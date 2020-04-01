package com.walkly.walkly.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Message

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val messages = mutableListOf<Message>()

    fun getChat(friendId: String, callback: (List<Message>) -> Unit) {
        db.collection("chats")
            .whereEqualTo("from", friendId)
            .whereEqualTo("to", friendId)
            .addSnapshotListener { snapshot, exception ->
                for (document in snapshot as QuerySnapshot){
                    messages.add(
                        document.toObject<Message>()
                    )
                }

                messages.sort()
                callback(messages)
            }
    }
}
