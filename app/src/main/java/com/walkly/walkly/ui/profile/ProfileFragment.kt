package com.walkly.walkly.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.auth.LoginActivity
import com.walkly.walkly.R
import kotlinx.android.synthetic.main.fragment_profile.*


@SuppressLint("Registered")
class ProfileFragment : Fragment() {
    lateinit var v: View
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_profile, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // showing user name
        val userName = auth.currentUser?.displayName

        if (userName != null) {
            tv_username.text = "Hello $userName"
        } else {
            tv_username.text = "error: could not retrieve user name"
            tv_username.setTextColor(Color.RED)
        }

        // click listeners

        tv_signout.setOnClickListener {
            signOut()
        }

        tv_view_leaderboard.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_home_to_leaderboardFragment)

        }

        tv_view_friends.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_friendsFragment)
        }

        tv_account_settings.setOnClickListener {
            view.findNavController().navigate(R.id.action_navigation_profile_to_accountSettingsFragment)
        }
    }



    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        updateUI()
    }

    private fun updateUI() {

        var intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

}