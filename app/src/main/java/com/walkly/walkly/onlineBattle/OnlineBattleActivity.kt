package com.walkly.walkly.onlineBattle

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.offlineBattle.ConsumablesBottomSheetDialog
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_online_battle.*
import kotlinx.android.synthetic.main.activity_online_battle.bar_enemy_hp
import kotlinx.android.synthetic.main.activity_online_battle.bar_player_hp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class OnlineBattleActivity : AppCompatActivity() {

    private val viewModel: OnlineBattleViewModel by lazy {
        ViewModelProviders.of(this).get(OnlineBattleViewModel::class.java)
    }

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_battle)

        supportActionBar?.hide()
        val id = intent.getStringExtra("battleId")
        viewModel.battleID = id!!
        viewModel.setUpListeners()

        loseDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_lost)
            .create()

        leaveDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_leave)
            .create()

        // TODO: construct dialog based real reward

        winDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_won)
            .create()

        // this doesn't work
//        consumablesBottomSheetDialog = ConsumablesBottomSheetDialog(this)
//
//        use_item.setOnClickListener {
//            consumablesBottomSheetDialog.show(supportFragmentManager, "consumableSheet")
//        }
//
//        viewModel.selectedConsumable.observe(this, Observer {
//            useConsumable(it.type, it.value)
//            viewModel.removeSelectedConsumable()
//        })

        viewModel.combinedHP.observe(this, Observer {
            bar_player_hp.progress = it.toInt()
            if (it <= 0) {
                loseDialog.show()
                loseDialog.findViewById<Button>(R.id.button1)
                    .setOnClickListener {
                        endGame()
                    }
            }
        })

        viewModel.enemyHP.observe(this, Observer {
            bar_enemy_hp.progress = it.toInt()
            if (it <= 0) {
//                enemy.level.value?.toInt()?.let { it1 -> Player.updatePoints(it1) }
//                getReward()
                winDialog.show()
                winDialog.findViewById<Button>(R.id.btn_collect)
                    .setOnClickListener {
                        endGame()
                    }
            }
        })

//        viewModel.walkedDistance.observe(this, androidx.lifecycle.Observer {
//            viewModel.currentEnemyHp -= it * 10
//            viewModel.enemyHpPercentage = ((currentEnemyHp * 100.0) / baseEnemyHP).toLong()
//            enemyHP.value = enemyHpPercentage
//        })

        startBattle()
    }

    private fun startBattle() {
        val util =
            DistanceUtil(this, Calendar.getInstance().timeInMillis, 500, viewModel.walkedDistance)
        util.startUpdates()
        scope.launch {
            viewModel.damagePlayer()
        }
    }

    private fun endGame() {
        val intent = Intent(this, MainActivity::class.java)
        viewModel.stopGame()
        startActivity(intent)
        finish()
    }

    private fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase()) {
            // add logic here
            "attack" -> bar_enemy_hp.progress -= consumableValue
            "health" -> bar_player_hp.progress += consumableValue
        }
    }
}
