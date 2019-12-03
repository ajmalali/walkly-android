package com.walkly.walkly.ui.leaderboard

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.walkly.walkly.R
import com.walkly.walkly.databinding.FragmentLeaderboardBinding

private const val TAG = "LeaderboardFragment"

class LeaderboardFragment : Fragment() {
    private lateinit var leaderboardViewModel: LeaderboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentLeaderboardBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_leaderboard, container, false)
        leaderboardViewModel = ViewModelProviders.of(this).get(LeaderboardViewModel::class.java)

        binding.lifecycleOwner = this

        val adapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.adapter = adapter

        leaderboardViewModel.globalLeaderboard.observe(this, Observer { list ->
            list?.let {
                Log.d(TAG, "Global leaderboard new list")
                adapter.submitList(list.toMutableList())
            }
        })

        leaderboardViewModel.friendsLeaderboard.observe(this, Observer { list ->
            list?.let {
                Log.d(TAG, "Friends leaderboard new list")
                adapter.submitList(list.toMutableList())
            }
        })

        binding.globalLeaderboardButton.setOnClickListener {
            leaderboardViewModel.getGlobalLeaderboard()
        }

        binding.friendsLeaderboardButton.setOnClickListener {
            leaderboardViewModel.getFriendsLeaderboard()
        }

        return binding.root
    }
}
