package com.walkly.walkly.offlineBattle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import kotlinx.android.synthetic.main.fragment_battle_activity.*

class OfflineBattle: AppCompatActivity() {

    private lateinit  var viewModel: OfflineBattleViewModel
    val enemy = Enemy("pxkYf10BTVnLDc7QWmhQ")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_battle_activity)

        viewModel = OfflineBattleViewModel(this, enemy)
        viewModel.startBattle()

        viewModel.playerHP.observe(this, Observer {
            playerHpBar.progress = it.toInt()
        })

        viewModel.enemyHP.observe(this, Observer {
            enemyHpBar.progress = it.toInt()
        })

        viewModel.enemyImage.observe(this, Observer {
            Glide.with(this)
                .load(it)
                .into(imageViewBoss)
        })

        // NOTE TESTED
        // Since starting time is now where the walked distance = 0
        viewModel.walkedDistance.observe(this, Observer {
            stepsBar.progress = it.toInt()
            Log.d("steps = ", it.toString())
        })



    }
}