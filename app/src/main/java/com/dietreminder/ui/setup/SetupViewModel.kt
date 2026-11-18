package com.dietreminder.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.alarm.AlarmScheduler
import com.dietreminder.data.database.entity.Goal
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.UserProfile
import com.dietreminder.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val setupComplete: StateFlow<Boolean?> = userRepository.observeProfile()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveSetup(
        name: String,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        goal: Goal,
        breakfastTime: String,
        lunchTime: String,
        dinnerTime: String,
        snacksTime: String
    ) {
        viewModelScope.launch {
            val profile = UserProfile(name = name, age = age, weightKg = weightKg, heightCm = heightCm, goal = goal)
            val schedule = MealSchedule(
                breakfastTime = breakfastTime,
                lunchTime = lunchTime,
                dinnerTime = dinnerTime,
                snacksTime = snacksTime
            )
            userRepository.saveProfile(profile)
            userRepository.saveSchedule(schedule)
            alarmScheduler.scheduleMealAlarms(schedule)
        }
    }
}
