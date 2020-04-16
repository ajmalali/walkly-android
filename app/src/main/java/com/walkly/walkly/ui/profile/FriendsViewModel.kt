package com.walkly.walkly.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.walkly.walkly.models.Friend

private const val TAG = "FriendsViewModel"

class FriendsViewModel : ViewModel() {

    private val _friendsList = MutableLiveData<List<Friend>>()
    val friendsList: LiveData<List<Friend>>
        get() = _friendsList

    private val _searchList = MutableLiveData<List<Friend>>()
    val searchList: LiveData<List<Friend>>
        get() = _searchList

    private val _selectedFriend = MutableLiveData<Friend>()
    val selectedFriend: LiveData<Friend>
        get() = _selectedFriend

    private var tempFriendList = mutableListOf<Friend>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    private val friendIds = mutableListOf<String>()
    private val friendStatus = mutableListOf<String>()


    init {
        getFriends()
    }

    fun getFriends() {
        db.collection("users")
            .document(userID!!)
            .collection("friends").orderBy("status", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    friendIds.add(document.id)
                    friendStatus.add((document.get("status") as String).replace("\"","") )
                    Log.d(TAG, friendStatus.toString())
                }
                // create the list
                createFriendList(friendIds)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting friends documents.", exception)
            }
    }

    // Creates the complete friend list and sets the value of friends
    private fun createFriendList(friendIds: List<String>) {
        // Pass friend ids in chunks of 10 to getFriendsFromIds
        val taskList = mutableListOf<Task<QuerySnapshot>>()
        for (idList in friendIds.chunked(10)) {
            taskList.add(getFriendsFromIds(idList))
        }

        Tasks.whenAllSuccess<Task<QuerySnapshot>>(taskList)
            .addOnSuccessListener {
                tempFriendList = tempFriendList.toMutableList()
                _friendsList.value = tempFriendList
                Log.d(TAG, "New friends list created: $tempFriendList")
            }
    }

    // Create a friend list from a given list of ids
    private fun getFriendsFromIds(idList: List<String>): Task<QuerySnapshot> {
        var statusCounter = 0;
        return db.collection("users")
            .whereIn(FieldPath.documentId(), idList)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item: Friend =
                        document.toObject(Friend::class.java).addIdAndStatus(document.id, friendStatus[statusCounter])
                    tempFriendList.add(item)
                    statusCounter++
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting friend documents.", exception)
            }
    }

    fun searchUser(userName: String) {
        db.collection("users")
            .whereGreaterThanOrEqualTo("name", userName)
            .whereLessThanOrEqualTo("name", "$userName\uF7FF")
            .get()
            .addOnSuccessListener { documents ->
                tempFriendList.clear()
                for (document in documents) {
                    val item: Friend =
                        document.toObject(Friend::class.java).addIdAndStatus(document.id, "")
                    tempFriendList.add(item)
                }

                _searchList.value = tempFriendList
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    fun selectFriend(f: Friend) {
        _selectedFriend.value = f
    }
}
