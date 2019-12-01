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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController

import com.walkly.walkly.R
import com.walkly.walkly.databinding.FragmentLeaderboardBinding

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

        leaderboardViewModel.leaderboardItems.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                adapter.submitList(list)
            }
        })

        return binding.root
    }
}
