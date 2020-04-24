package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Friend

object FriendsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users = mutableListOf<Friend>()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDoc = db.collection("users").document(uid)
    private val friends = mutableListOf<String>()
    private val friendRquests = mutableListOf<String>()

    init {
        userDoc.addSnapshotListener { snapshot, exception ->
            friends.clear()
            friendRquests.clear()
            try {
                friends.addAll(snapshot?.data?.get("friends") as List<String>)
                friendRquests.addAll(snapshot.data?.get("friendRequests") as List<String>)
            } catch (tce: TypeCastException) {
                return@addSnapshotListener
            }
        }
    }

    // TODO: Uncomment general search (after DB clean up)
    fun search(name: String, callback: (List<Friend>) -> Unit) {
        db.collection("users")
            .whereEqualTo("name", name)
//            .whereGreaterThanOrEqualTo("name", name)
//            .whereLessThanOrEqualTo("name", "$name\uF7FF")
            .get()
            .addOnSuccessListener {
                users.clear()
                for (document in it) {
                    val friend = document.toObject<Friend>().addId(document.id)
                    // TODO: check pending, accepted or else
                    friend.type = 2
                    users.add(friend)
                }
                callback(users)
            }
    }

    fun getFriends(callback: (List<Friend>) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener {
                users.clear()
                for (document in it) {
                    // TODO: check pending or accepted
                    if (friends.contains(document.id)) {
                        val friend = document.toObject<Friend>().addId(document.id)

                        friend.type = 1
                        users.add(friend)
                    } else if (friendRquests.contains(document.id)) {
                        val friend = document.toObject<Friend>().addId(document.id)

                        friend.type = 0
                        users.add(friend)
                    }
                }
                callback(users)
            }
    }

    fun addFriend(id: String, callback: (Boolean) -> Unit) {
        userDoc.update(
            "friends", FieldValue.arrayUnion(id)
        ).addOnSuccessListener {
            db.collection("users")
                .document(id)
                .update(
                    "friendRequests", FieldValue.arrayUnion(uid)
                )
                .addOnSuccessListener {
                    callback(true)
                }
        }
    }

    fun acceptFriend(id: String, callback: (Boolean) -> Unit) {
        userDoc.update(
            "friends", FieldValue.arrayUnion(id),
            "friendRequests", FieldValue.arrayRemove(id)
        ).addOnSuccessListener {
            callback(true)
        }
    }

    fun rejectFriend(id: String, callback: (Boolean) -> Unit) {
        userDoc.update(
            "friendRequests", FieldValue.arrayRemove(id)
        ).addOnSuccessListener {
            db.collection("users")
                .document(id)
                .update(
                    "friends", FieldValue.arrayRemove(uid)
                ).addOnSuccessListener {
                    callback(true)
                }
        }
    }
}