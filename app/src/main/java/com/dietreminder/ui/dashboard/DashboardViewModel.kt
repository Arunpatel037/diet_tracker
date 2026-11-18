package com.dietreminder.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.MealType
import com.dietreminder.data.database.entity.Medicine
import com.dietreminder.data.database.entity.UserProfile
import com.dietreminder.data.repository.MealRepository
import com.dietreminder.data.repository.MedicineRepository
import com.dietreminder.data.repository.UserRepository
import com.dietreminder.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val profile: UserProfile? = null,
    val schedule: MealSchedule? = null,
    val todayMeals: List<MealHistory> = emptyList(),
    val medicines: List<Medicine> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val mealRepository: MealRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    // Refreshes at midnight so the dashboard always shows the correct day's meals.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val todayMealsFlow: Flow<List<MealHistory>> =
        TimeUtils.currentDateFlow().flatMapLatest { date ->
            mealRepository.observeByDate(date)
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        userRepository.observeProfile(),
        userRepository.observeSchedule(),
        todayMealsFlow,
        medicineRepository.observeAll()
    ) { profile, schedule, meals, medicines ->
        DashboardUiState(profile = profile, schedule = schedule, todayMeals = meals, medicines = medicines)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
