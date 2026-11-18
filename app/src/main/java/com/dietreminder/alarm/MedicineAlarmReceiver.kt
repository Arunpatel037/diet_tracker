package com.dietreminder.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.dietreminder.MainActivity
import com.dietreminder.data.repository.MedicineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicineAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var medicineRepository: MedicineRepository

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getLongExtra(AlarmScheduler.EXTRA_MEDICINE_ID, -1L)
        if (medicineId == -1L) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Channel already created in DietReminderApp.onCreate(); no need to recreate here.
        val channelId = "medicine_reminders"

        val tapIntent = PendingIntent.getActivity(
            context, medicineId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medicine Reminder")
            .setContentText("Time to take your medicine")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(notifSoundUri)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        nm.notify(medicineId.toInt(), notification)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medicine = medicineRepository.getActive().find { it.id == medicineId }
                medicine?.let { alarmScheduler.scheduleMedicineAlarm(it) }
            } finally {
                pending.finish()
            }
        }
    }
}
