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
import com.google.firebase.firestore.ktx.toObjects
import com.walkly.walkly.models.Friend
import com.walkly.walkly.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "FriendsViewModel"

class FriendsViewModel : ViewModel() {

    private val currentPlayer = PlayerRepository.getPlayer()

    private val _friendsList = MutableLiveData<List<Friend>>()
    val friendsList: LiveData<List<Friend>>
        get() = _friendsList

    private val _searchList = MutableLiveData<List<Friend>>()
    val searchList: LiveData<List<Friend>>
        get() = _searchList

    private var tempFriendList = mutableListOf<Friend>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    init {
        getFriends()
    }

    private fun getFriends() {
//        createFriendList(currentPlayer.friends)
        db.collection("users").document(userID!!).get()
            .addOnSuccessListener { document ->
                try {
                    CoroutineScope(IO).launch {
                        createFriendList(document.data?.get("friends") as MutableList<String>)
                    }
                } catch (e: TypeCastException) {
                    _friendsList.value = mutableListOf()
                }
            }
    }

    // Creates the complete friend list and sets the value of friends
    private suspend fun createFriendList(friendIds: List<String>) {
        // Pass friend ids in chunks of 10 to getFriendsFromIds

        for (idList in friendIds.sorted().chunked(10)) {
            val friends = db.collection("users")
                .whereIn(FieldPath.documentId(), idList)
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
                .get().await().toObjects<Friend>()

            Log.d(TAG, "Sorted IDs $idList")
            for (i in friends.indices) {
                tempFriendList.add(friends[i].addId(idList[i]))
            }

            Log.d(TAG, "Temp list IDs $tempFriendList")
        }

        tempFriendList = tempFriendList.toMutableList()
        _friendsList.postValue(tempFriendList)
        Log.d(TAG, "New friends list created: $tempFriendList")
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
}
