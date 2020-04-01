package com.walkly.walkly.repositories

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Message

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
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
                        document.toObject<Message>()
                            .addAvatar(friendAvatar)
                    )
                }

                db.collection("chats")
                    .whereEqualTo("to", friendId)
                    .addSnapshotListener { snapshot, exception ->
                        for (document in snapshot as QuerySnapshot){
                            messages.add(
                                document.toObject<Message>()
                                    .addAvatar(friendAvatar).also {
                                        it.sent = true
                                    }
                            )
                        }

                        messages.sort()
                        callback(messages)
                    }


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

    fun sendMessage(to: String, text: String, callback: (Boolean) -> Unit){
        db.collection("chats").document()
            .set(mapOf(
               "from" to uid,
                "to"  to to,
                "text" to text,
                "time" to FieldValue.serverTimestamp()
            ))
            .addOnSuccessListener {
                callback(true)
            }
    }
}
