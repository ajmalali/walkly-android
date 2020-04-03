package com.walkly.walkly.ui.lobby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.onlineBattle.OnlineBattleActivity
import kotlinx.android.synthetic.main.activity_online_lobby.*

private const val TAG = "OnlineLobbyActivity"

class OnlineLobbyActivity : AppCompatActivity() {

    private val viewModel: LobbyViewModel by viewModels()
    private var playerCount: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_lobby)
        supportActionBar!!.hide()

        val battle = intent.getParcelableExtra<OnlineBattle>("battle")
        battle?.let {
            setupEnemy(battle.enemy!!)
            updatePlayers(battle.players)
            viewModel.setupPlayersListener(battle.id!!)
            if (battle.players[0].id == viewModel.userID) {
                displayBattleControls()
            } else {
                displayWaiting()
            }
        }

        start_button.isClickable = false

        viewModel.playerList.observe(this, Observer { list ->
            list?.let {
                playerCount = list.size
                if (playerCount > 1) {
                    start_button.isClickable = true
                }
                updatePlayers(list)
            }
        })

        start_button.setOnClickListener {
            val intent = Intent(this, OnlineBattleActivity::class.java)
            val bundle = Bundle()
            intent.putExtras(bundle)
            startActivity(intent)
            this.finish()
        }

//        opponent_avatar.setOnClickListener {
//            viewModel.invFriend(intent.getStringExtra("battleId"))
//        }
    }

    private fun displayBattleControls() {
        start_button.visibility = View.VISIBLE
        publicize_button.visibility = View.VISIBLE
        loading_bar.visibility = View.GONE
        tv_waiting_lobby.visibility = View.GONE
    }

    private fun displayWaiting() {
        start_button.visibility = View.GONE
        publicize_button.visibility = View.GONE
        loading_bar.visibility = View.VISIBLE
        tv_waiting_lobby.visibility = View.VISIBLE
    }

    private fun setupPlayer(
        player: BattlePlayer,
        nameTextView: TextView,
        avatarImage: ImageView,
        equipmentImage: ImageView
    ) {
        nameTextView.text = player.name

        Glide.with(this)
            .load(player.avatarURL)
            .into(avatarImage)

        Glide.with(this)
            .load(player.equipmentURL)
            .into(equipmentImage)
    }

    private fun updatePlayers(players: List<BattlePlayer>) {
        if (playerCount == 4) {
            setupPlayer(
                players[3],
                tv_player3_name_lobby,
                img_player3_avatar_lobby,
                img_player3_equipment_lobby
            )
        }

        if (playerCount == 3) {
            setupPlayer(
                players[2],
                tv_player2_name_lobby,
                img_player2_avatar_lobby,
                img_player2_equipment_lobby
            )
        }

        if (playerCount == 2) {
            setupPlayer(
                players[1],
                tv_player1_name_lobby,
                img_player1_avatar_lobby,
                img_player1_equipment_lobby
            )
        }

        if (playerCount == 1) {
            setupPlayer(
                players[0],
                current_player_name,
                current_player_avatar,
                current_player_equipment_lobby
            )
        }
    }

    private fun setupEnemy(enemy: Enemy) {
        tv_enemy_name.text = enemy.name
        val health = "Health: ${enemy.health}"
        tv_enemy_health.text = health
        val level = "Level: ${enemy.level}"
        tv_enemy_level.text = level

        Glide.with(this)
            .load(enemy.image)
            .into(enemy_image)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
}
