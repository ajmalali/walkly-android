package com.walkly.walkly.ui.leaderboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "LeaderboardViewModel"

class LeaderboardViewModel : ViewModel() {

    // To prevent access from outside, leaderboardItems is set to read-only
    private val _leaderboardItems = MutableLiveData<List<LeaderboardItem>>()
    val leaderboardItems: LiveData<List<LeaderboardItem>>
        get() = _leaderboardItems

    private var list = mutableListOf<LeaderboardItem>()

    init {
        getTopTenUsers()
    }

    // Populates the leaderboard with top 10 users based on points
    fun getTopTenUsers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item: LeaderboardItem = document.toObject(LeaderboardItem::class.java).addId(document.id)
                    list.add(item)

                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                _leaderboardItems.value = list
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}
