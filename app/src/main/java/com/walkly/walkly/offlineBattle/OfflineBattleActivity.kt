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
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Player
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.fragment_battle_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class OfflineBattleActivity : AppCompatActivity() {

    val viewModel: OfflineBattleViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog
    private val walkedDistance = MutableLiveData<Float>()

    private lateinit var distanceUtil: DistanceUtil

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.fragment_battle_activity)

        // TODO: Refactor this (Use Parcelables)
        val bundle = intent.extras
        var enemy: Enemy? = null
        bundle?.let {
            enemy = Enemy(
                bundle.getLong("enemyLvl"),
                bundle.getString("enemyId")!!,
                bundle.getLong("enemyHP"),
                bundle.getLong("enemyDmg")
            )
        }

        enemy?.let { viewModel.initEnemy(it) }

        distanceUtil = DistanceUtil(this, walkedDistance)

        consumablesBottomSheetDialog = ConsumablesBottomSheetDialog(this)

        use_items.setOnClickListener {
            consumablesBottomSheetDialog.show(supportFragmentManager, "consumableSheet")
        }

        viewModel.selectedConsumable.observe(this, Observer {
            useConsumable(it.type, it.value)
            viewModel.removeSelectedConsumable()
        })


        viewModel.playerHP.observe(this, Observer {
//            player_health_bar.progress = it.toInt()
            player_health_bar.setProgress(it, true)
            if (it <= 0) {
                loseDialog.show()
                loseDialog.findViewById<Button>(R.id.button1)
                    .setOnClickListener {
                        endGame()
                    }
            }
        })

        viewModel.enemyHP.observe(this, Observer {
//            enemy_health_bar.progress = it.toInt()
            enemy_health_bar.setProgress(it, true)
            // If game ends
            if (it <= 0) {
//                enemy.level.value?.toInt()?.let { it1 -> Player.updatePoints(it1) }
                getReward()
                winDialog.show()
                winDialog.findViewById<Button>(R.id.btn_collect)
                    .setOnClickListener {
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
            leaveDialog.show()
            leaveDialog.findViewById<Button>(R.id.btn_leave)
                .setOnClickListener {
                    endGame()
                }
            leaveDialog.findViewById<Button>(R.id.btn_stay)
                .setOnClickListener {
                    leaveDialog.dismiss()
                }
        }

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

        startBattle()
    }

    private fun getReward() {

    }

    private fun startBattle() {
        scope.launch {
            viewModel.damagePlayer()
        }
    }

    private fun endGame() {
        job.complete()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun useConsumable(consumableType: String, consumableValue: Int) {
        when (consumableType.toLowerCase(Locale.ROOT)) {
            "attack" -> enemy_health_bar.progress -= consumableValue
            "health" -> player_health_bar.progress += consumableValue
        }
    }
}