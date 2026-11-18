package com.dietreminder.ui.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.repository.MealRepository
import com.dietreminder.util.ImageUtils
import com.dietreminder.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mealRepository: MealRepository
) : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Refreshes at midnight so the 30-day window always tracks the real current date.
    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<MealHistory>> = TimeUtils.currentDateFlow().flatMapLatest { todayStr ->
        val thirtyDaysAgo = LocalDate.parse(todayStr, formatter).minusDays(29)
        mealRepository.observeRange(
            from = thirtyDaysAgo.format(formatter),
            to = todayStr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(TimeUtils.today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val selectedDayMeals: StateFlow<List<MealHistory>> = combine(history, selectedDate) { all, date ->
        all.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: String) { _selectedDate.value = date }

    fun cleanOldData() {
        viewModelScope.launch {
            val cutoffDate = TimeUtils.cutoffDate(30)
            mealRepository.deleteOlderThan(cutoffDate)
            // Delete photo files older than 30 days to prevent unbounded disk usage.
            val cutoffMs = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            ImageUtils.deleteOldPhotos(context, cutoffMs)
        }
    }
}
