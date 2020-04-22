package com.walkly.walkly.onlineBattle

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.models.OnlineBattle
import com.walkly.walkly.repositories.PlayerRepository
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_online_battle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

private const val TAG = "OnlineBattleActivity"

class OnlineBattleActivity : AppCompatActivity() {

    private val viewModel: OnlineBattleViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var winInflater: View

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog

    private val walkedDistance = MutableLiveData<Float>()
    private lateinit var distanceUtil: DistanceUtil

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var playerCount: Int = 1
    private var damageFlag = true
    private var masterID: String = "" // Can only make the edits
    private var battleEnded = false

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
                masterID = list[0].id
                startPlayerDamage(list)
            }
        })

        viewModel.combinedHP.observe(this, Observer {
            Log.d(TAG, "Combined player health: $it")
            bar_player_hp.progress = it.toInt()
            if (it <= 0) {
                loseDialog.show()
            }
        })

        viewModel.enemyHP.observe(this, Observer {
            bar_enemy_hp.progress = it.toInt()
            if (it <= 0 && !battleEnded) {
                battleEnded = true
                CoroutineScope(IO).launch {
                    val equipment = viewModel.getReward()
                    withContext(Main) {
                        setupReward(equipment)
                        winDialog.show()
                    }
                }

                PlayerRepository.updatePoints(1)
            }
        })

        viewModel.totalSteps.observe(this, Observer {
            val neededSteps = "$it / ${battle?.enemy?.health}"
            tv_no_of_steps.text = neededSteps
        })

        walkedDistance.observe(this, Observer {
            Log.d(TAG, "Steps: $it")
            it?.let {
                scope.launch {
                    viewModel.damageEnemy(it.toLong())
                }
            }
        })

        btn_leave.setOnClickListener {
            leaveDialog.show()
        }

        viewModel.setupBattleListener()
        viewModel.setupEnemyHealthListener()
    }

    private fun setupReward(equipment: Equipment) {
        Glide.with(this)
            .load(equipment.image)
            .into(winInflater.findViewById(R.id.item_image))

        winInflater.findViewById<TextView>(R.id.item_name).text = equipment.name
        winInflater.findViewById<TextView>(R.id.item_level).text = "Level: ${equipment.level}"
        winInflater.findViewById<TextView>(R.id.item_value).text = "+ ${equipment.value} damage"
    }

    // Minimize the app
    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

    private fun setupEnemy(enemy: Enemy) {
        viewModel.baseEnemyHP = enemy.health!!
        viewModel.enemyDamage = enemy.damage!!
        Glide.with(this)
            .load(enemy.image)
            .into(img_enemy_avatar)
    }

    private fun setupPlayer(
        player: BattlePlayer?,
        nameTextView: TextView,
        avatarImage: ImageView,
        equipmentImage: ImageView
    ) {
        if (player != null) {
            equipmentImage.visibility = View.VISIBLE
            nameTextView.text = player.name

            Glide.with(this)
                .load(player.avatarURL)
                .into(avatarImage)

            Glide.with(this)
                .load(player.equipmentURL)
                .into(equipmentImage)
        } else {
            nameTextView.text = "Waiting"
            avatarImage.setImageResource(R.drawable.ic_account_circle_black_24dp)
            equipmentImage.visibility = View.INVISIBLE
        }
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
        } else {
            setupPlayer(
                null,
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
        } else {
            setupPlayer(
                null,
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
        } else {
            setupPlayer(
                null,
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
        if (players[0].id == viewModel.userID && damageFlag) {
            damageFlag = false
            CoroutineScope(IO).launch {
                Log.d(TAG, "Starting player damage")
                viewModel.damagePlayer()
            }
        }
    }

    private fun initDialogs() {
        // Leave Dialog
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby, null)
        leaveDialog = AlertDialog.Builder(this)
            .setView(leaveInflater)
            .create()
        leaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        leaveInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                CoroutineScope(IO).launch {
                    viewModel.removeCurrentPlayer()
                    withContext(Dispatchers.Main) {
                        leaveDialog.dismiss()
                        finish()
                    }
                }
            }

        leaveInflater.findViewById<Button>(R.id.btn_stay)
            .setOnClickListener {
                leaveDialog.dismiss()
            }

        // Win Dialog
        winInflater = layoutInflater.inflate(R.layout.dialog_battle_won, null)
        winDialog = AlertDialog.Builder(this)
            .setView(winInflater)
            .create()

        winInflater.findViewById<Button>(R.id.btn_collect)
            .setOnClickListener {
                winDialog.dismiss()
                endGame()
            }

        winDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

        loseDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        if (masterID == viewModel.userID) {
            CoroutineScope(IO).launch {
                viewModel.changeBattleState("Finished")
            }
        }

        viewModel.stopGame()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
