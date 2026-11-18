package com.dietreminder

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DietReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        // Only delete+recreate when channel config changes (bump CHANNEL_VERSION to apply new settings).
        // Deleting on every launch causes OxygenOS to block the channel on recreation.
        val CHANNEL_VERSION = 1
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getInt("channel_version", 0) != CHANNEL_VERSION) {
            nm.deleteNotificationChannel("meal_alarms")
            nm.deleteNotificationChannel("medicine_reminders")
            prefs.edit().putInt("channel_version", CHANNEL_VERSION).apply()
        }

        // Meal alarm channel — ringtone stream so it behaves like an incoming call.
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarmAudioAttrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        val mealChannel = NotificationChannel(
            "meal_alarms", "Meal Alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Full-screen meal reminder alarms"
            setSound(alarmUri, alarmAudioAttrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
            setBypassDnd(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        // Medicine reminder channel — standard notification stream.
        val notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notifAudioAttrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val medicineChannel = NotificationChannel(
            "medicine_reminders", "Medicine Reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Medicine reminder notifications"
            setSound(notifUri, notifAudioAttrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }

        nm.createNotificationChannels(listOf(mealChannel, medicineChannel))
    }
}
