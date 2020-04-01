package com.walkly.walkly.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Friend

object FriendsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users = mutableListOf<Friend>()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDoc = db.collection("users").document(uid)
    private val friends = mutableListOf<String>()

    init {
        userDoc.addSnapshotListener { snapshot, exception ->
            friends.addAll(0, snapshot?.data?.get("friends") as List<String>)
        }
    }

    fun search(name: String, callback: (List<Friend>) -> Unit){
        db.collection("users")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener {
                users.clear()
                for (document in it){
                    val friend = document.toObject<Friend>().addId(document.id)
                    // TODO: check pending, accepted or else
                    friend.type = 2
                    users.add(friend)
                }
                callback(users)
            }
    }

    fun getFriends(callback: (List<Friend>) -> Unit){
        db.collection("users")
            .get()
            .addOnSuccessListener {
                users.clear()
                for (document in it){
                    if (friends.contains(document.id)){
                        val friend = document.toObject<Friend>().addId(document.id)
                        // TODO: check pending or accepted
                        friend.type = 1
                        users.add(friend)
                    }
                }
                callback(users)
            }
    }
}