package com.walkly.walkly.offlineBattle

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.walkly.walkly.models.Enemy
import java.lang.IllegalArgumentException

class OfflineBattleViewModelFactory(private val activity: AppCompatActivity, private val enemy: Enemy) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineBattleViewModel::class.java)) {
            return OfflineBattleViewModel(activity, enemy) as T
        }
        throw IllegalArgumentException("unknown viewModel class")
    }
}