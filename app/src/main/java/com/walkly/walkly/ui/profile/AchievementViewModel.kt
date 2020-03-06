package com.walkly.walkly.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.walkly.walkly.models.Achievement
import com.walkly.walkly.repositories.AchievementsRepository
import com.walkly.walkly.repositories.AchievementsRepository.getAchievements

class AchievementViewModel : ViewModel() {
    private val _achievemnetsList = MutableLiveData<List<Achievement>>()
    val achievemnetsList: LiveData<List<Achievement>>
        get() = _achievemnetsList
    init {
        getAchievements()
    }

    private fun getAchievements() {
        if (_achievemnetsList.value != null) {
            _achievemnetsList.value = AchievementsRepository.achievementList
        } else {
            getAchievements { list ->
                _achievemnetsList.value = list
            }
        }
    }
}