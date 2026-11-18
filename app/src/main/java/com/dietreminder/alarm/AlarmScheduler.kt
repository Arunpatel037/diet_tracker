package com.dietreminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dietreminder.MainActivity
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.MealType
import com.dietreminder.data.database.entity.Medicine
import com.dietreminder.util.TimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMealAlarms(schedule: MealSchedule) {
        scheduleMeal(MealType.BREAKFAST, schedule.breakfastTime)
        scheduleMeal(MealType.LUNCH, schedule.lunchTime)
        scheduleMeal(MealType.DINNER, schedule.dinnerTime)
        scheduleMeal(MealType.SNACKS, schedule.snacksTime)
    }

    fun cancelMealAlarms() {
        MealType.entries.forEach { type ->
            val intent = mealIntent(type)
            alarmManager.cancel(intent)
        }
    }

    private fun scheduleMeal(type: MealType, time: String) {
        val (hour, minute) = TimeUtils.parseHHmm(time)
        val triggerAt = TimeUtils.nextAlarmMillis(hour, minute)
        setExactAlarm(mealIntent(type), triggerAt)
    }

    // Called from MealAlarmReceiver after the alarm fires to set up tomorrow's alarm.
    fun scheduleMealForTomorrow(type: MealType, time: String) {
        val (hour, minute) = TimeUtils.parseHHmm(time)
        val triggerAt = TimeUtils.tomorrowAlarmMillis(hour, minute)
        setExactAlarm(mealIntent(type), triggerAt)
    }

    private fun setExactAlarm(intent: PendingIntent, triggerAt: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            // setAlarmClock is never batched/deferred by Doze or OEM battery savers, unlike setExactAndAllowWhileIdle.
            val showIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showIntent), intent)
        }
    }

    private fun mealIntent(type: MealType): PendingIntent {
        val intent = Intent(context, MealAlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEAL_TYPE, type.name)
        }
        return PendingIntent.getBroadcast(
            context,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleMedicineAlarm(medicine: Medicine) {
        val (hour, minute) = TimeUtils.parseHHmm(medicine.reminderTime)
        val triggerAt = TimeUtils.nextAlarmMillis(hour, minute)
        setExactAlarm(medicineIntent(medicine.id), triggerAt)
    }

    fun cancelMedicineAlarm(medicineId: Long) {
        alarmManager.cancel(medicineIntent(medicineId))
    }

    private fun medicineIntent(medicineId: Long): PendingIntent {
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDICINE_ID, medicineId)
        }
        return PendingIntent.getBroadcast(
            context,
            MEDICINE_REQUEST_BASE + medicineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_MEAL_TYPE = "extra_meal_type"
        const val EXTRA_MEDICINE_ID = "extra_medicine_id"
        private const val MEDICINE_REQUEST_BASE = 1000
    }
}
