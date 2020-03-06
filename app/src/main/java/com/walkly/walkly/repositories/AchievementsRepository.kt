package com.walkly.walkly.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.models.Achievement

private const val TAG = "ConsumableRepository"

// Singleton repository object
object AchievementsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userID: String = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private val userDocument = db.collection("users").document(userID)
    val achievementList = mutableListOf<Achievement>()

    // Get consumables of the current user
    fun getAchievements(callback: (List<Achievement>) -> Unit) {
        achievementList.clear()
        db.collection("achievements")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val ach = document.toObject(Achievement::class.java).addId(document.id)
                    achievementList.add(ach)
                    Log.d(TAG, "Added $document")
                }
                userDocument.collection("achievements")
                    .addSnapshotListener { result, e ->
                        Log.d(TAG, "List before: $achievementList")
                        for (document in result!!) {
                            val it: MutableIterator<Achievement> = achievementList.iterator()
                            while (it.hasNext()) {
                                val ach: Achievement = it.next()
                                if (ach.id == document.id) {
                                    ach.earned = true
                                    Log.d(TAG, "earned ${ach.earned}")
                                }
                            }
                        }
                        achievementList.sortBy { it.earned.not() }
                        Log.d(TAG, "List After: $achievementList")
                        callback(achievementList)
                    }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

}
