package com.dietreminder.ui.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dietreminder.alarm.AlarmScheduler
import com.dietreminder.alarm.MealAlarmReceiver
import com.dietreminder.data.database.entity.MealType
import com.dietreminder.ui.theme.DietReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealAlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and wake the display
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val mealTypeName = intent.getStringExtra(AlarmScheduler.EXTRA_MEAL_TYPE) ?: MealType.BREAKFAST.name
        val mealType = runCatching { MealType.valueOf(mealTypeName) }.getOrElse { MealType.BREAKFAST }

        // Block back-press — user must capture a photo to dismiss
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { /* intentionally blocked */ }
        })

        startRingtone()

        enableEdgeToEdge()
        setContent {
            DietReminderTheme {
                val photoSaved by viewModel.photoSaved.collectAsState()
                if (photoSaved) {
                    LaunchedEffect(Unit) {
                        stopRingtone()
                        cancelMealNotification(mealType)
                        finish()
                    }
                }
                AlarmScreen(
                    mealType = mealType,
                    onPhotoCapture = { path -> viewModel.completeMeal(mealType, path) }
                )
            }
        }
    }

    private fun startRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(applicationContext, uri)
                isLooping = true
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        } catch (e: Exception) {
            // Don't crash the alarm screen if audio setup fails
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.runCatching { if (isPlaying) stop() }
        mediaPlayer?.runCatching { release() }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    private fun cancelMealNotification(mealType: MealType) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(MealAlarmReceiver.NOTIF_ID_BASE + mealType.ordinal)
    }
}

@Composable
fun AlarmScreen(mealType: MealType, onPhotoCapture: (String) -> Unit) {
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        CameraScreen(
            onPhotoCaptured = { path ->
                onPhotoCapture(path)
                showCamera = false
            },
            onCancel = { showCamera = false }
        )
    } else {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Time for ${mealType.name.lowercase().replaceFirstChar { it.uppercase() }}!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Take a photo of your meal to mark it complete.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = { showCamera = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Capture Meal Photo", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "You must capture a photo to dismiss this reminder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
