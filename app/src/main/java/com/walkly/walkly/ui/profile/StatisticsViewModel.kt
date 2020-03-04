package com.walkly.walkly.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.walkly.walkly.models.Friend

private const val TAG = "StatisticsViewModel"

class StatisticsViewModel : ViewModel() {

    private val _steps = MutableLiveData<Long>()
    val steps: LiveData<Long>
        get() = _steps

    private val _enemies = MutableLiveData<Long>()
    val enemies: LiveData<Long>
        get() = _enemies

    private val _raids = MutableLiveData<Long>()
    val raids: LiveData<Long>
        get() = _raids

    private val _distance = MutableLiveData<Long>()
    val distance: LiveData<Long>
        get() = _distance



    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid
    val userRef = db.collection("users").document(uid!!)


    init {
        getStatistics()
    }

    fun getStatistics() {
        userRef.addSnapshotListener { snapshot, firebaseFirestoreException ->
            _steps.value = snapshot!!.getLong("steps")
            _enemies.value = snapshot!!.getLong("enemies")
            _raids.value = snapshot!!.getLong("raids")
            _distance.value = calcDistance(steps)
        }
    }

    // Calculate Distance by Steps
    private fun calcDistance(steps: LiveData<Long>) : Long {
        // 1 km = 1,250 steps
        return steps.value!!/1250
    }

}
