package com.walkly.walkly.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.walkly.walkly.R
import kotlin.math.absoluteValue

private const val TAG = "StatisticsFragment"

class StatisticsFragment : Fragment()  {

    private val statisticsViewModel: StatisticsViewModel by lazy {
        ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)

        var enemiesView: TextView = view.findViewById(R.id.tv_enemeies)
        var raidsView: TextView = view.findViewById(R.id.tv_raids)
        var distanceView: TextView = view.findViewById(R.id.tv_distance)
        var stepsView: TextView = view.findViewById(R.id.tv_steps)

        statisticsViewModel.steps.observe(this, Observer { steps ->
            steps?.let {
                stepsView.setText("$it \n Steps Walked")
            }
        })

        statisticsViewModel.enemies.observe(this, Observer {enemies ->
            enemies?.let {
                enemiesView.setText("$it \n Enemies Defeated")
            }
        })

        statisticsViewModel.raids.observe(this, Observer {raids ->
            raids?.let {
                raidsView.setText("$it \n Raids Defeated")
            }
        })

        statisticsViewModel.distance.observe(this, Observer {distance ->
            distance?.let {
                distanceView.setText("$it \n Km Walked")
            }
        })

        return view
    }
}
