package com.walkly.walkly.ui.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.R
import com.walkly.walkly.models.Achievement

private const val TAG = "AchievementFragment"

class AchievementFragment : Fragment(), AchievementAdapter.OnAchievementUseListener {

    // Imported db stuff for the onClick method, this should be moved outside the fragment
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userID = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var achievementRecyclerView: RecyclerView
    private lateinit var errorMessage: TextView

    private var achievementList = mutableListOf<Achievement>()
    private lateinit var adapter: AchievementAdapter

    private val achievementViewModel: AchievementViewModel by lazy {
        ViewModelProviders.of(this).get(AchievementViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        achievementRecyclerView = view.findViewById(R.id.achievement_recyclerView)


        adapter = AchievementAdapter(
            achievementList,
            this
        )
        achievementRecyclerView.adapter = adapter

        errorMessage = view.findViewById(R.id.error_achievement)

        achievementViewModel.achievemnetsList.observe(this, Observer { list ->
            list?.let {
                if (list.isEmpty()) {
                    errorMessage.text = getString(R.string.no_friends)
                    errorMessage.visibility = View.VISIBLE
                } else {
                    errorMessage.visibility = View.GONE
                    adapter.achievementList = list
                    Log.d("ach Frag", "list is: $list")
                    adapter.notifyDataSetChanged()
                }
            }
        })
        return view
    }

    override fun onAchievementClick(i: Int){

    }

}
