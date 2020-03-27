package com.walkly.walkly.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.walkly.walkly.models.Quest

object QuestsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val quests = mutableListOf<Quest>()

    fun getQuests(callback: (List<Quest>) -> Unit){
        db.collection("quests")
            .get()
            .addOnSuccessListener {
                for (document in it){
                    val quest = document.toObject<Quest>()
                    quests.add(quest)
                }
                callback(quests)
            }
    }
}