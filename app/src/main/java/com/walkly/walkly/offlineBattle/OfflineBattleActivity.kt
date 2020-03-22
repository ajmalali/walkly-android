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
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_battle_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*

private const val TAG = "OfflineBattleActivity"

class OfflineBattleActivity : AppCompatActivity() {

    private val viewModel: OfflineBattleViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog
    private val walkedDistance = MutableLiveData<Float>()

    private lateinit var distanceUtil: DistanceUtil
    private var steps = 0
    private var pauseTime = -1L

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.fragment_battle_activity)

        initDialogs()

        leaveBattle.setOnClickListener {
            leaveDialog.show()
        }

        val bundle = intent.extras
        // From notification
        when {
            bundle?.getString("result") == "lose" -> {
                loseDialog.show()
            }
            bundle?.getString("result") == "win" -> {
                winDialog.show()
            }
            else -> {
                initEnemy(bundle)
                initHealthListeners()
                initConsumables()

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

                startBattle()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.battleEnded) {
            Intent(this, BackgroundOfflineBattleService::class.java).also {
                this.stopService(it)
            }

            distanceUtil = DistanceUtil(this, walkedDistance)

            val damageMultiples = (Date().time - pauseTime) / viewModel.HIT_FREQUENCY
            if (damageMultiples > 0) {
                val playerHppercentage =
                    ((viewModel.currentPlayerHP * 100) / viewModel.basePlayerHP)
                viewModel.playerHP.value = playerHppercentage.toInt()
            }

            scope.launch {
                val steps = distanceUtil.getStepsUntil(pauseTime)
                if (steps != null && steps != -1) {
                    val text = "$steps"
                    tv_no_of_steps.text = text
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val extras = OfflineServiceInfo(
            viewModel.currentEnemyHp.toInt(), viewModel.enemyDamage.toInt(),
            viewModel.currentPlayerHP.toInt(), 1, viewModel.HIT_FREQUENCY
        )

        pauseTime = Date().time

        if (!viewModel.battleEnded) {
            Intent(this, BackgroundOfflineBattleService::class.java).also {
                it.putExtra("info", extras)
                this.startService(it)
            }
        }
    }

    private fun initDialogs() {
        // Leave Dialog
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_battle_leave, container)
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
        val winInflater = layoutInflater.inflate(R.layout.dialog_battle_won, container)
        winDialog = AlertDialog.Builder(this)
            .setView(winInflater)
            .create()

        winInflater.findViewById<Button>(R.id.btn_collect)
            .setOnClickListener {
                winDialog.dismiss()
                endGame()
            }

        // Lose Dialog
        val loseInflater = layoutInflater.inflate(R.layout.dialog_battle_lost, container)
        loseDialog = AlertDialog.Builder(this)
            .setView(loseInflater)
            .create()

        loseInflater.findViewById<Button>(R.id.go_home)
            .setOnClickListener {
                loseDialog.dismiss()
                endGame()
            }
    }

    private fun initEnemy(bundle: Bundle?) {
        // TODO: Refactor this (Use Parcelables)
        var enemy: Enemy? = null
        bundle?.let {
            enemy = Enemy(
                bundle.getString("enemyLvl")?.toLong()!!,
                bundle.getString("enemyId")!!,
                bundle.getLong("enemyHP"),
                bundle.getLong("enemyDmg")
            )
        }

        enemy?.let { viewModel.initEnemy(it) }
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
    }

    private fun initConsumables() {
        consumablesBottomSheetDialog = ConsumablesBottomSheetDialog(this)
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
    }

    @RequiresApi(Build.VERSION_CODES.N) // Only for animation
    private fun initHealthListeners() {
        viewModel.playerHP.observe(this, Observer {
//            player_health_bar.progress = it.toInt()
            player_health_bar.setProgress(it, true)
            if (it <= 0) {
                viewModel.battleEnded = true
                loseDialog.show()

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
            }
        })
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

    class OfflineServiceInfo(
        val enemyHealth: Int, val enemyPower: Int,
        val playerHealth: Int, val playerPower: Int, val frequency: Long
    ) : Serializable

}