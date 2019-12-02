package com.walkly.walkly.ui.leaderboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "LeaderboardViewModel"

class LeaderboardViewModel : ViewModel() {

    // To prevent access from outside, both leaderboards are set to read-only
    private val _globalLeaderboard = MutableLiveData<List<LeaderboardItem>>()
    val globalLeaderboard: LiveData<List<LeaderboardItem>>
        get() = _globalLeaderboard

    private val _friendsLeaderboard = MutableLiveData<List<LeaderboardItem>>()
    val friendsLeaderboard: LiveData<List<LeaderboardItem>>
        get() = _friendsLeaderboard

    private var list = mutableListOf<LeaderboardItem>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        getGlobalLeaderboard()
    }

    // Populates the leaderboard with top 10 users based on points
    fun getGlobalLeaderboard() {
        if (_globalLeaderboard.value == null) {
            db.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val item: LeaderboardItem =
                            document.toObject(LeaderboardItem::class.java).addId(document.id)
                        list.add(item)

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    _globalLeaderboard.value = list
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        } else {
            _globalLeaderboard.value = list
        }
    }

    fun getFriendsLeaderboard() {

    }
}
