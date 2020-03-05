package com.walkly.walkly.ui.lobby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.walkly.walkly.R
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import kotlinx.android.synthetic.main.fragment_lobby.*
import kotlinx.android.synthetic.main.list_host_battles.*

class LobbyActivity : AppCompatActivity() {
    private lateinit var viewModel: LobbyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_lobby)
        val battleID = intent.getStringExtra("battleId")
        viewModel = ViewModelProviders.of(this).get(LobbyViewModel::class.java)

        setupHeader()

        start_button.setOnClickListener {
            val intent = Intent(this, OnlineBattleActivity::class.java)
            val bundle = Bundle()
            bundle.putString("battleId", battleID)
            intent.putExtras(bundle)
            startActivity(intent)
            this.finish()
        }

        img_player1_avatar_lobby.setOnClickListener {
            viewModel.invFriend(intent.getStringExtra("battleId"))

        }
    }
    private fun setupHeader(){
        supportActionBar!!.hide()
        tv_enemy_name.text =  intent.getStringExtra("enemyName")
        tv_enemy_health.text =  "HP: ${intent.getStringExtra("enemyHP")}"
        tv_enemy_level.text =  "HP: ${intent.getStringExtra("enemyLvl")}"
    }
}
