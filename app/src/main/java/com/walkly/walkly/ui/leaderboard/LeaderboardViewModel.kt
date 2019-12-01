package com.walkly.walkly.ui.leaderboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "LeaderboardViewModel"

class LeaderboardViewModel : ViewModel() {

    // To prevent access from outside, leaderboardItems can not be changed directly
    private val _leaderboardItems = MutableLiveData<List<LeaderboardItem>>()
    val leaderboardItems: LiveData<List<LeaderboardItem>>
        get() = _leaderboardItems

    private var list = mutableListOf<LeaderboardItem>()

    init {
        getTopTen()
    }

    // Populates the leaderboard with top 10 users based on points
    private fun getTopTen() {
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
                _leaderboardItems.value = list
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}
