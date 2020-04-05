package com.walkly.walkly.onlineBattle

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.BattlePlayer
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_online_battle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "OnlineBattleActivity"

class OnlineBattleActivity : AppCompatActivity() {

    private val viewModel: OnlineBattleViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog

    private val walkedDistance = MutableLiveData<Float>()
    private lateinit var distanceUtil: DistanceUtil

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var playerCount: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_battle)
        supportActionBar?.hide()

        initDialogs()
        initConsumables()

        val battle = intent.getParcelableExtra<OnlineBattle>("battle")
        battle?.let {
            updatePlayers(battle.players)
            setupEnemy(battle.enemy!!)
            playerCount = battle.playerCount!!
            viewModel.battleID = battle.id!!
        }

        distanceUtil = DistanceUtil(this, walkedDistance)

        viewModel.playerList.observe(this, Observer { list ->
            list?.let {
                playerCount = list.size
                updatePlayers(list as MutableList<BattlePlayer>)
            }
        })

        viewModel.combinedHP.observe(this, Observer {
            Log.d("HERE", "$it")
            bar_player_hp.progress = it.toInt()
            if (it <= 0) {
                loseDialog.show()
            }
        })

        viewModel.enemyHP.observe(this, Observer {
            bar_enemy_hp.progress = it.toInt()
            if (it <= 0) {
//                enemy.level.value?.toInt()?.let { it1 -> Player.updatePoints(it1) }
//                getReward()
                winDialog.show()
            }
        })

        walkedDistance.observe(this, Observer {
            it?.let {
                scope.launch {
                    viewModel.damageEnemy(it.toLong())
                }
            }
        })

        viewModel.setupBattleListener()
        viewModel.setupEnemyHealthListener()
        startPlayerDamage(battle?.players!!)
    }

    private fun setupEnemy(enemy: Enemy) {
        viewModel.baseEnemyHP = enemy.health!!
        viewModel.enemyDamage = enemy.damage!!
        Glide.with(this)
            .load(enemy.image)
            .into(img_enemy_avatar)
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

    private fun updatePlayers(players: MutableList<BattlePlayer>) {
        // TODO: Show empty first
        if (players.size == 4) {
            setupPlayer(
                players[3],
                tv_player4_name,
                img_player4_avatar,
                img_player4_equipment
            )
        }

        if (players.size >= 3) {
            setupPlayer(
                players[2],
                tv_player3_name,
                img_player3_avatar,
                img_player3_equipment
            )
        }

        if (players.size >= 2) {
            setupPlayer(
                players[1],
                tv_player2_name,
                img_player2_avatar,
                img_player2_equipment
            )
        }

        if (players.size >= 1) {
            setupPlayer(
                players[0],
                tv_player1_name,
                img_player1_avatar,
                img_player1_equipment
            )
        }
    }

    // Only the player at position 0 can call damagePlayer.
    // This will work even if the host leaves the battle mid-way.
    private fun startPlayerDamage(players: MutableList<BattlePlayer>) {
        Log.d(TAG, "List of players: $players")
        if (players[0].id == viewModel.userID) {
            CoroutineScope(IO).launch {
                Log.d(TAG, "Starting player damage")
                viewModel.damagePlayer()
            }
        }
    }

    private fun initDialogs() {
        // Leave Dialog
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_battle_leave, null)
        leaveDialog = AlertDialog.Builder(this)
            .setView(leaveInflater)
            .create()
        leaveInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                leaveDialog.dismiss()
                viewModel.battleEnded = true
                endGame()
            }
        leaveInflater.findViewById<Button>(R.id.btn_stay)
            .setOnClickListener {
                leaveDialog.dismiss()
            }

        // Win Dialog
        val winInflater = layoutInflater.inflate(R.layout.dialog_battle_won, null)
        winDialog = AlertDialog.Builder(this)
            .setView(winInflater)
            .create()

        winInflater.findViewById<Button>(R.id.btn_collect)
            .setOnClickListener {
                winDialog.dismiss()
                endGame()
            }

        winDialog.setCancelable(false)
        winDialog.setCanceledOnTouchOutside(false)

        // Lose Dialog
        val loseInflater = layoutInflater.inflate(R.layout.dialog_battle_lost, null)
        loseDialog = AlertDialog.Builder(this)
            .setView(loseInflater)
            .create()

        loseInflater.findViewById<Button>(R.id.go_home)
            .setOnClickListener {
                loseDialog.dismiss()
                endGame()
            }

        loseDialog.setCancelable(false)
        loseDialog.setCanceledOnTouchOutside(false)

    }

    private fun initConsumables() {
        consumablesBottomSheetDialog = ConsumablesBottomSheetDialog(this)
        consumablesViewModel.selectedConsumable.observe(this, Observer {
            viewModel.useConsumable(it.type, it.value)
            consumablesViewModel.removeSelectedConsumable()
        })

        use_item.setOnClickListener {
            consumablesBottomSheetDialog.show(
                supportFragmentManager,
                ConsumablesBottomSheetDialog.TAG
            )
        }
    }

    private fun endGame() {
        val intent = Intent(this, MainActivity::class.java)
        viewModel.stopGame()
        startActivity(intent)
        finish()
    }
}
