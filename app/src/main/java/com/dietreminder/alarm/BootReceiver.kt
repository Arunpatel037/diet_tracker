package com.dietreminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dietreminder.data.repository.MedicineRepository
import com.dietreminder.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var medicineRepository: MedicineRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule = userRepository.getSchedule()
                schedule?.let { alarmScheduler.scheduleMealAlarms(it) }
                val medicines = medicineRepository.getActive()
                medicines.forEach { alarmScheduler.scheduleMedicineAlarm(it) }
            } finally {
                pending.finish()
            }
        }
    }
}
