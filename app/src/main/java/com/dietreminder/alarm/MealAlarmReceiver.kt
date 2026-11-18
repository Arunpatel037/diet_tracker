package com.dietreminder.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.dietreminder.data.database.entity.MealType
import com.dietreminder.data.repository.UserRepository
import com.dietreminder.ui.alarm.MealAlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MealAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var userRepository: UserRepository

    override fun onReceive(context: Context, intent: Intent) {
        val mealTypeName = intent.getStringExtra(AlarmScheduler.EXTRA_MEAL_TYPE) ?: return
        val mealType = runCatching { MealType.valueOf(mealTypeName) }.getOrNull() ?: return

        // Full-screen intent: works from background/lock screen on all API levels.
        // On API 29+ startActivity() from background is blocked; this is the correct approach.
        val activityIntent = Intent(context, MealAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_MEAL_TYPE, mealTypeName)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context,
            FULL_SCREEN_REQUEST_BASE + mealType.ordinal,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val label = mealTypeName.lowercase().replaceFirstChar { it.uppercase() }
        val notification = NotificationCompat.Builder(context, "meal_alarms")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ $label Time!")
            .setContentText("Take a photo of your meal to log it.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmSoundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
            .setFullScreenIntent(fullScreenPi, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_BASE + mealType.ordinal, notification)

        // Reschedule this alarm for the same time tomorrow (one-shot alarms don't repeat).
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val schedule = userRepository.getSchedule()
                schedule?.let { s ->
                    val time = when (mealType) {
                        MealType.BREAKFAST -> s.breakfastTime
                        MealType.LUNCH     -> s.lunchTime
                        MealType.DINNER    -> s.dinnerTime
                        MealType.SNACKS    -> s.snacksTime
                    }
                    alarmScheduler.scheduleMealForTomorrow(mealType, time)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val NOTIF_ID_BASE = 100
        private const val FULL_SCREEN_REQUEST_BASE = 200
    }
}
