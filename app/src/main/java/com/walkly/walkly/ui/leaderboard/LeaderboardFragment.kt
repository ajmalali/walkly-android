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
        binding.progressBar.visibility = View.VISIBLE
        binding.lifecycleOwner = this

        val adapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.adapter = adapter

        leaderboardViewModel = ViewModelProviders.of(this).get(LeaderboardViewModel::class.java)

        leaderboardViewModel.globalLeaderboard.observe(this, Observer { list ->
            list?.let {
                Log.d(TAG, "Global leaderboard list observed")
                // clear the list for the adapter
//                adapter.submitList(null)
                binding.progressBar.visibility = View.GONE
                adapter.submitList(list)
            }
        })

        leaderboardViewModel.friendsLeaderboard.observe(this, Observer { list ->
            list?.let {
                Log.d(TAG, "Friends leaderboard list observed")
//                adapter.submitList(null)
                binding.progressBar.visibility = View.GONE
                adapter.submitList(list)
            }
        })

        binding.globalLeaderboardButton.setOnClickListener {
            adapter.submitList(null)
            binding.progressBar.visibility = View.VISIBLE
            leaderboardViewModel.getGlobalLeaderboard()
            binding.leaderboardRecyclerView.smoothScrollToPosition(0)
        }

        binding.friendsLeaderboardButton.setOnClickListener {
            adapter.submitList(null)
            binding.progressBar.visibility = View.VISIBLE
            leaderboardViewModel.getFriendsLeaderboard()
            binding.leaderboardRecyclerView.smoothScrollToPosition(0)
        }

        return binding.root
    }
}
