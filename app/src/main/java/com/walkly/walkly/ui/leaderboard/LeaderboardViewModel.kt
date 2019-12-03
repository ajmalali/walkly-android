package com.walkly.walkly.ui.leaderboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
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

    private var tempGlobalList = mutableListOf<LeaderboardItem>()
    private var tempFriendList = mutableListOf<LeaderboardItem>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    private val friendIds = mutableListOf<String>()

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
                        tempGlobalList.add(item)

                    }
                    Log.d(TAG, "New global leaderboard created")
                    _globalLeaderboard.value = tempGlobalList
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting global leaderboard documents.", exception)
                }
        } else {
            _globalLeaderboard.value = tempGlobalList
        }
    }

    /*
        Gets all friend IDs from the currently logged-in user.
        Then creates the friend leaderboard list, 10 friends at a time because
        the list in whereIn("id", list) query can only hold 10 items. This approach
        does not require duplication of user documents and so can decrease the overall
        number of reads and writes.
     */
    fun getFriendsLeaderboard() {
        if (_friendsLeaderboard.value == null) {
            db.collection("users")
                .document(userID!!)
                .collection("friends")
                .get()
                .addOnSuccessListener { result ->
                    friendIds.add(userID)
                    for (document in result) {
                        friendIds.add(document.id)
                    }

                    Log.d(TAG, "Friend IDs: $friendIds")
                    // create the leaderboard
                    createFriendLeaderboard(friendIds)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting friends leaderboard documents.", exception)
                }
        } else {
            _friendsLeaderboard.value = tempFriendList
        }

    }

    // Creates the complete friends leaderboard and sets the value of friendsLeaderboard
    private fun createFriendLeaderboard(friendIds: List<String>) {
        // Pass friend ids in chunks of 10 to getLeaderboardFromIds
        for (idList in friendIds.chunked(10)) {
            getLeaderboardFromIds(idList)
        }

        tempFriendList = tempFriendList.sorted().toMutableList()
        _friendsLeaderboard.value = tempFriendList
        Log.d(TAG, "New friends leaderboard created: $tempFriendList")
    }

    // Create a leaderboard from a given list of ids
    private fun getLeaderboardFromIds(idList: List<String>) {
        db.collection("users")
            .whereIn(FieldPath.documentId(), idList)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item: LeaderboardItem =
                        document.toObject(LeaderboardItem::class.java).addId(document.id)
                    tempFriendList.add(item)
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting global leaderboard documents.", exception)
            }
    }
}
