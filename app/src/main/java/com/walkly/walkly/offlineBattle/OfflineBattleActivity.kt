package com.walkly.walkly.offlineBattle

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.fragment_battle_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "OfflineBattleActivity"

class OfflineBattleActivity : AppCompatActivity() {

    private val viewModel: OfflineBattleViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog
    private val walkedDistance = MutableLiveData<Float>()

    var steps = 0
    private lateinit var distanceUtil: DistanceUtil

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.fragment_battle_activity)

        winDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_won)
            .create()
        loseDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_battle_lost)
            .create()

        // TODO: Refactor this (Use Parcelables)
        val bundle = intent.extras
        var enemy: Enemy? = null

        if (bundle?.getString("result") == "lose") {
            loseDialog.show()
            loseDialog.findViewById<Button>(R.id.button1)
                .setOnClickListener {
                    endGame()
                }
        } else if (bundle?.getString("result") == "win") {
            winDialog.show()
            winDialog.findViewById<Button>(R.id.btn_collect)
                .setOnClickListener {
                    endGame()
                }
        } else {

            lateinit var enemy: Enemy
            bundle?.let {
                enemy = Enemy(
                    bundle.getString("enemyLvl")?.toLong()!!,
                    bundle.getString("enemyId")!!,
                    bundle.getLong("enemyHP"),
                    bundle.getLong("enemyDmg")
                )
            }

            enemy?.let { viewModel.initEnemy(it) }

            distanceUtil = DistanceUtil(this, walkedDistance)

            consumablesBottomSheetDialog =
                ConsumablesBottomSheetDialog(this)

            consumablesViewModel.selectedConsumable.observe(this, Observer {
                viewModel.useConsumable(it.type, it.value)
                consumablesViewModel.removeSelectedConsumable()
            })

            use_items.setOnClickListener {
                consumablesBottomSheetDialog.show(
                    supportFragmentManager,
                    ConsumablesBottomSheetDialog.TAG
                )
            }

            viewModel.playerHP.observe(this, Observer {
//            player_health_bar.progress = it.toInt()
                player_health_bar.setProgress(it, true)
                if (it <= 0) {
                    loseDialog.show()
                    viewModel.battleEnded = true
                    loseDialog.findViewById<Button>(R.id.button1)
                        .setOnClickListener {
                            loseDialog.dismiss()
                            endGame()
                        }
                }
            })

            // TODO: Fix points
            viewModel.enemyHP.observe(this, Observer {
//            enemy_health_bar.progress = it.toInt()
                enemy_health_bar.setProgress(it, true)
                // If game ends
                if (it <= 0) {
//                enemy.level.value?.toInt()?.let { it1 -> Player.updatePoints(it1) }
                    getReward()
                    viewModel.battleEnded = true
                    winDialog.show()
                    winDialog.findViewById<Button>(R.id.btn_collect)
                        .setOnClickListener {
                            winDialog.dismiss()
                            endGame()
                        }
                }
            })

            // TODO: Use enemy image
            // get enemy image
//        val imagename = enemy.image.toString()
            val imagename = "boss" + bundle?.getString("enemyId")
            boss_bitmoji.setImageResource(
                resources.getIdentifier(
                    imagename,
                    "drawable",
                    this.packageName
                )
            )

            // NOT TESTED
            // Since starting time is now where the walked distance = 0

            // TODO: Change to Enemy Health / player equipment
            var steps = 0
            var totalSteps = "$steps / 1000"
            tv_no_of_steps.text = totalSteps

            walkedDistance.observe(this, Observer {
                viewModel.damageEnemy(it)
                steps += it.toInt()
                totalSteps = "$steps / 1000"
                tv_no_of_steps.text = totalSteps
                Log.d("steps = ", it.toString())
            })

            leaveBattle.setOnClickListener {
                viewModel.battleEnded = true
                leaveDialog.show()
                leaveDialog.findViewById<Button>(R.id.btn_leave)
                    .setOnClickListener {
                        leaveDialog.dismiss()
                        endGame()
                    }
                leaveDialog.findViewById<Button>(R.id.btn_stay)
                    .setOnClickListener {
                        leaveDialog.dismiss()
                    }
            }



            leaveDialog = AlertDialog.Builder(this)
                .setView(R.layout.dialog_battle_leave)
                .create()

            startBattle()
        }
    }

    private fun getReward() {
        // TODO: construct win dialog based real reward

    }

    private fun startBattle() {
        scope.launch {
            viewModel.damagePlayer()
        }
    }

    private fun endGame() {
        job.cancel()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}