package com.walkly.walkly.offlineBattle

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    val enemy = Enemy("pxkYf10BTVnLDc7QWmhQ")
    private lateinit var loseDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_battle_activity)

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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        loseDialog = AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("you lost the game")
            .setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, id ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            })
            .create()


    }
}