package com.walkly.walkly.ui.leaderboard

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_leaderboard.*

private const val TAG = "LeaderboardFragment"

class LeaderboardFragment : Fragment() {

    var leaderboardList = mutableListOf<String>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        leaderboardList = mutableListOf()
        initLeaderboard()
    }

    private fun initLeaderboard() {
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    leaderboardList.add("${document.data["name"]} = ${document.data["points"]}")
                    Log.d(TAG, leaderboardList.toString())
                }
                leaderboard_1.text = leaderboardList[0]
                leaderboard_2.text = leaderboardList[1]
                leaderboard_3.text = leaderboardList[2]
                leaderboard_4.text = leaderboardList[3]
                leaderboard_5.text = leaderboardList[4]
                leaderboard_6.text = leaderboardList[5]
                leaderboard_7.text = leaderboardList[6]
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

}
