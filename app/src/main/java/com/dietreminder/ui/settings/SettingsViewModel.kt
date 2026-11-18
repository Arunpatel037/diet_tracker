package com.dietreminder.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.alarm.AlarmScheduler
import com.dietreminder.data.database.AppDatabase
import com.dietreminder.data.repository.MealRepository
import com.dietreminder.data.repository.MedicineRepository
import com.dietreminder.data.repository.UserRepository
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.UserProfile
import com.dietreminder.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val mealRepository: MealRepository,
    private val medicineRepository: MedicineRepository,
    private val alarmScheduler: AlarmScheduler,
    private val database: AppDatabase
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val schedule: StateFlow<MealSchedule?> = userRepository.observeSchedule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch { userRepository.saveProfile(profile) }
    }

    fun updateSchedule(schedule: MealSchedule) {
        viewModelScope.launch {
            userRepository.saveSchedule(schedule)
            alarmScheduler.cancelMealAlarms()
            alarmScheduler.scheduleMealAlarms(schedule)
        }
    }

    fun resetAll(onComplete: () -> Unit) {
        viewModelScope.launch {
            alarmScheduler.cancelMealAlarms()
            medicineRepository.getActive().forEach { alarmScheduler.cancelMedicineAlarm(it.id) }
            database.clearAllTables()
            ImageUtils.deleteOldPhotos(context, Long.MAX_VALUE)
            onComplete()
        }
    }
}
