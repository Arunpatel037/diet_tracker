package com.dietreminder.ui.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dietreminder.alarm.AlarmScheduler
import com.dietreminder.data.database.entity.Medicine
import com.dietreminder.data.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val medicines: StateFlow<List<Medicine>> = medicineRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val id = medicineRepository.save(medicine)
            val saved = medicine.copy(id = if (medicine.id == 0L) id else medicine.id)
            if (saved.isActive) alarmScheduler.scheduleMedicineAlarm(saved)
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            alarmScheduler.cancelMedicineAlarm(medicine.id)
            medicineRepository.delete(medicine)
        }
    }
}
