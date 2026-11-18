package com.dietreminder.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.database.entity.MealType
import com.dietreminder.data.repository.MealRepository
import com.dietreminder.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val mealRepository: MealRepository
) : ViewModel() {

    private val _photoSaved = MutableStateFlow(false)
    val photoSaved: StateFlow<Boolean> = _photoSaved

    fun completeMeal(mealType: MealType, photoPath: String) {
        viewModelScope.launch {
            val today = TimeUtils.today()
            if (mealRepository.getByDateAndType(today, mealType) == null) {
                mealRepository.saveMealCompletion(
                    MealHistory(
                        date = today,
                        mealType = mealType,
                        completedAt = System.currentTimeMillis(),
                        photoPath = photoPath
                    )
                )
            }
            _photoSaved.value = true
        }
    }
}
