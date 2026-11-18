package com.dietreminder.ui.setup

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dietreminder.data.database.entity.Goal
import com.dietreminder.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }

    // Profile fields
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf(Goal.MAINTENANCE) }

    // Meal times
    var breakfastTime by remember { mutableStateOf("08:00") }
    var lunchTime by remember { mutableStateOf("13:00") }
    var dinnerTime by remember { mutableStateOf("19:00") }
    var snacksTime by remember { mutableStateOf("16:00") }

    fun doFinishSetup() {
        val ageInt = age.toIntOrNull() ?: 0
        val weightF = weight.toFloatOrNull() ?: 0f
        val heightF = height.toFloatOrNull() ?: 0f
        viewModel.saveSetup(name, ageInt, weightF, heightF, goal,
            breakfastTime, lunchTime, dinnerTime, snacksTime)
        onSetupComplete()
    }

    // Permission chain (each launcher calls the next one in sequence):
    // exactAlarm → notification → camera → batteryOpt → finish
    // Using separate launchers prevents multiple system dialogs from racing each other.

    val batteryOptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { doFinishSetup() }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        val pm = context.getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            batteryOptLauncher.launch(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}"))
            )
        } else {
            doFinishSetup()
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> cameraPermLauncher.launch(Manifest.permission.CAMERA) }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Setup — Step ${page + 1} of 3") })
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (page) {
                0 -> ProfilePage(
                    name = name, onName = { name = it },
                    age = age, onAge = { age = it },
                    weight = weight, onWeight = { weight = it },
                    height = height, onHeight = { height = it },
                    goal = goal, onGoal = { goal = it },
                    onNext = { if (name.isNotBlank() && age.isNotBlank()) page++ }
                )
                1 -> MealTimePage(
                    breakfastTime = breakfastTime, onBreakfast = { breakfastTime = it },
                    lunchTime = lunchTime, onLunch = { lunchTime = it },
                    dinnerTime = dinnerTime, onDinner = { dinnerTime = it },
                    snacksTime = snacksTime, onSnacks = { snacksTime = it },
                    onNext = { page++ }
                )
                2 -> PermissionsPage(
                    onRequestPermissions = {
                        // Start the chain: exact alarm first (needs a settings page on Android 12+),
                        // then notification, then camera, then battery optimization.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                        ) {
                            exactAlarmLauncher.launch(
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}"))
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onFinish = { doFinishSetup() }
                )
            }
        }
    }
}

@Composable
private fun ProfilePage(
    name: String, onName: (String) -> Unit,
    age: String, onAge: (String) -> Unit,
    weight: String, onWeight: (String) -> Unit,
    height: String, onHeight: (String) -> Unit,
    goal: Goal, onGoal: (Goal) -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tell us about you", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = onName, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = age, onValueChange = onAge, label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = weight, onValueChange = onWeight, label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = height, onValueChange = onHeight, label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        Text("Your Goal", style = MaterialTheme.typography.labelLarge)
        GoalSelector(selected = goal, onSelect = onGoal)
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank() && age.isNotBlank()) {
            Text("Next")
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun GoalSelector(selected: Goal, onSelect: (Goal) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Goal.entries.forEach { g ->
            FilterChip(
                selected = g == selected,
                onClick = { onSelect(g) },
                label = { Text(g.name.replace("_", " ")) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTimePage(
    breakfastTime: String, onBreakfast: (String) -> Unit,
    lunchTime: String, onLunch: (String) -> Unit,
    dinnerTime: String, onDinner: (String) -> Unit,
    snacksTime: String, onSnacks: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Set Meal Times", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        TimePickerRow("Breakfast", breakfastTime, onBreakfast)
        TimePickerRow("Lunch", lunchTime, onLunch)
        TimePickerRow("Dinner", dinnerTime, onDinner)
        TimePickerRow("Snacks", snacksTime, onSnacks)
        Spacer(Modifier.weight(1f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerRow(label: String, time: String, onTimeChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val (h, m) = TimeUtils.parseHHmm(time)
    val state = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(TimeUtils.formatHHmm(state.hour, state.minute))
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) }
        )
    }

    OutlinedCard(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(TimeUtils.displayTime(time), style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.AccessTime, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PermissionsPage(onRequestPermissions: () -> Unit, onFinish: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Permissions Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        PermissionCard("Notifications", "Get meal and medicine reminders")
        PermissionCard("Camera", "Capture meal photos to confirm completion")
        PermissionCard("Exact Alarms", "Deliver reminders at the exact scheduled time")
        PermissionCard("Battery Optimization", "Prevent the OS from killing alarms in the background (required on OnePlus / Xiaomi / Samsung)")
        Spacer(Modifier.weight(1f))
        Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) { Text("Grant Permissions") }
        OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Skip & Finish") }
    }
}

@Composable
private fun PermissionCard(title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
