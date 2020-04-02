package com.walkly.walkly.ui.lobby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.walkly.walkly.R
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import kotlinx.android.synthetic.main.activity_online_lobby.*

class OnlineLobbyActivity : AppCompatActivity() {
    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_lobby)

        val battleID = intent.getStringExtra("battleId")

        setupHeader()

        start_button.setOnClickListener {
            val intent = Intent(this, OnlineBattleActivity::class.java)
            val bundle = Bundle()
            bundle.putString("battleId", battleID)
            intent.putExtras(bundle)
            startActivity(intent)
            this.finish()
        }

        opponent_avatar.setOnClickListener {
            viewModel.invFriend(intent.getStringExtra("battleId"))
        }
    }
    private fun setupHeader(){
        supportActionBar!!.hide()
        tv_enemy_name.text =  intent.getStringExtra("enemyName")
        tv_enemy_health.text =  "HP: ${intent.getStringExtra("enemyHP")}"
        tv_enemy_level.text =  "Level: ${intent.getStringExtra("enemyLvl")}"
    }
}
