package com.walkly.walkly.offlineBattle

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Equipment
import com.walkly.walkly.repositories.PlayerRepository
import com.walkly.walkly.ui.consumables.ConsumablesBottomSheetDialog
import com.walkly.walkly.ui.consumables.ConsumablesViewModel
import com.walkly.walkly.utilities.DistanceUtil
import kotlinx.android.synthetic.main.activity_offline_battle.*
import kotlinx.android.synthetic.main.activity_offline_battle.bar_enemy_hp
import kotlinx.android.synthetic.main.activity_offline_battle.bar_player_hp
import kotlinx.android.synthetic.main.activity_offline_battle.btn_leave
import kotlinx.android.synthetic.main.activity_offline_battle.img_enemy_avatar
import kotlinx.android.synthetic.main.activity_offline_battle.tv_no_of_steps
import kotlinx.android.synthetic.main.activity_offline_battle.use_item
import kotlinx.android.synthetic.main.activity_online_battle.*
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.*

private const val TAG = "OfflineBattleActivity"

class OfflineBattleActivity : AppCompatActivity() {

    private val viewModel: OfflineBattleViewModel by viewModels()
    private val consumablesViewModel: ConsumablesViewModel by viewModels()

    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog
    private lateinit var winDialog: AlertDialog
    private lateinit var winInflater: View

    private lateinit var consumablesBottomSheetDialog: ConsumablesBottomSheetDialog
    private val walkedDistance = MutableLiveData<Float>()

    private lateinit var distanceUtil: DistanceUtil
    private var pauseTime = -1L

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        // To remove status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_offline_battle)

        initDialogs()

        btn_leave.setOnClickListener {
            leaveDialog.show()
        }

        Glide.with(this)
            .load(viewModel.currentPlayer.photoURL)
            .into(img_player_avatar)

        Glide.with(this)
            .load(viewModel.currentPlayer.currentEquipment?.image)
            .into(img_player_equipment)

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
                initEnemy()
                initHealthListeners()
                initConsumables()

                var totalSteps = "0 / ${viewModel.requiredSteps}"
                tv_no_of_steps.text = totalSteps
                walkedDistance.observe(this, Observer {
                    viewModel.damageEnemy(it)
                    totalSteps = "${viewModel.stepsTaken} / ${viewModel.requiredSteps}"
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
        val leaveInflater = layoutInflater.inflate(R.layout.dialog_leave_lobby, null)
        leaveDialog = AlertDialog.Builder(this)
            .setView(leaveInflater)
            .create()
        leaveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        leaveInflater.findViewById<Button>(R.id.btn_leave)
            .setOnClickListener {
                leaveDialog.dismiss()
                finish()
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

    private fun initEnemy() {
        val enemy: Enemy? = intent.getParcelableExtra("enemy")

        enemy?.let { viewModel.initEnemy(it) }
        // get enemy image
        Glide.with(this)
            .load(enemy?.image)
            .into(img_enemy_avatar)
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

    @RequiresApi(Build.VERSION_CODES.N) // Only for animation
    private fun initHealthListeners() {
        viewModel.playerHP.observe(this, Observer {
//            player_health_bar.progress = it.toInt()
            bar_player_hp.setProgress(it, true)
            if (it <= 0) {
                viewModel.battleEnded = true
                loseDialog.show()

            }
        })

        // TODO: Fix points
        viewModel.enemyHP.observe(this, Observer {
//            enemy_health_bar.progress = it.toInt()
            bar_enemy_hp.setProgress(it, true)
            // If game ends
            if (it <= 0) {
                walkedDistance.removeObservers(this@OfflineBattleActivity)
                CoroutineScope(Dispatchers.IO).launch {
                    val equipment = viewModel.getReward()
                    withContext(Dispatchers.Main) {
                        setupReward(equipment)
                        winDialog.show()
                    }
                }

                PlayerRepository.updatePoints(viewModel.enemyLevel)
            }
        })
    }

    private fun setupReward(equipment: Equipment) {
        Glide.with(this)
            .load(equipment.image)
            .into(winInflater.findViewById(R.id.item_image))

        winInflater.findViewById<TextView>(R.id.item_name).text = equipment.name
        winInflater.findViewById<TextView>(R.id.item_level).text = "Level: ${equipment.level}"
        winInflater.findViewById<TextView>(R.id.item_value).text =
            "+ ${equipment.value} ${equipment.type}"
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