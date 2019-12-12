package com.walkly.walkly.offlineBattle

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.ui.battleactivity.ConsumablesBottomSheetDialog
import kotlinx.android.synthetic.main.fragment_battle_activity.*

class OfflineBattle: AppCompatActivity() {

    private lateinit  var viewModel: OfflineBattleViewModel


    private lateinit var loseDialog: AlertDialog
    private lateinit var leaveDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_battle_activity)

        val bundle = intent.extras
        val enemy = Enemy(bundle.getString("enemyId"))

        use_items.setOnClickListener {
            Toast.makeText(this, "coming in 418", Toast.LENGTH_LONG)
                .show()
        }

        viewModel = OfflineBattleViewModel(this, enemy)
        viewModel.startBattle()

        viewModel.playerHP.observe(this, Observer {
            player_health_bar.progress = it.toInt()
            if (it <= 0){
                loseDialog.show()
            }
        })

        viewModel.enemyHP.observe(this, Observer {
            enemy_health_bar.progress = it.toInt()
        })

        viewModel.enemyImage.observe(this, Observer {
            Glide.with(this)
                .load(it)
                .into(boss_bitmoji)
        })

        // NOTE TESTED
        // Since starting time is now where the walked distance = 0
        viewModel.walkedDistance.observe(this, Observer {
            player_step_bar.progress = it.toInt()
            Log.d("steps = ", it.toString())
        })

        leaveBattle.setOnClickListener {
            leaveDialog.show()
        }

        loseDialog = AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("you lost the game")
            .setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id ->
                endGame()
            })
            .create()

        leaveDialog = AlertDialog.Builder(this)
            .setTitle("Leaving The Battle")
            .setMessage("Are you sure? You will lose progress.")
            .setPositiveButton("Leave", DialogInterface.OnClickListener{ dialog, id ->
                endGame()
            })
            .setNegativeButton("Stay", DialogInterface.OnClickListener{dialog, id ->
                dialog.dismiss()
            })
            .create()

    }
    fun endGame(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}