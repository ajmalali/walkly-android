package com.walkly.walkly.ui.leaderboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "LeaderboardViewModel"

class LeaderboardViewModel : ViewModel() {

    val leaderboardItems = MutableLiveData<List<LeaderboardItem>>()
    private var list = mutableListOf<LeaderboardItem>()

    init {
        initLeaderboard()
    }

    /*
        Populates the leaderboard with top 10 users based on points
     */
    private fun initLeaderboard() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    list.add(document.toObject(LeaderboardItem::class.java))
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                leaderboardItems.value = list
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}
