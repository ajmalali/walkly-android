package com.walkly.walkly.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Quest

object QuestsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val quests = mutableListOf<Quest>()
    private val userDoc = db.collection("users").document(
        FirebaseAuth.getInstance().currentUser?.uid.toString()
    )
    private val completedQuests = mutableListOf<String>()

    init {
        userDoc.addSnapshotListener { snapshot, exception ->
            completedQuests.addAll(0, snapshot?.data?.get("CompletedQuests") as List<String>)
        }
    }

    fun getQuests(callback: (List<Quest>) -> Unit){
        db.collection("quests")
            .get()
            .addOnSuccessListener {
                for (document in it){
                    if (!completedQuests.contains(document.id)) {
                        val quest = document.toObject<Quest>().addId(document.id)
                        quests.add(quest)
                    }
                }
                callback(quests)
            }
    }

    fun completeQuest(quest: Quest, callback: (Boolean) -> Unit){
        userDoc.update(
            "CompletedQuests", FieldValue.arrayUnion(quest.id)
        ).addOnSuccessListener {
            quests.remove(quest)
            callback(true)
        }

    }

}