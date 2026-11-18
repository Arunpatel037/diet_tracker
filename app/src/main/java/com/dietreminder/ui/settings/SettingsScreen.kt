package com.dietreminder.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dietreminder.data.database.entity.Goal
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.UserProfile
import com.dietreminder.ui.setup.TimePickerRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onResetComplete: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Application") },
            text = { Text("This will delete all your data including meal history, medicines, and photos. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAll { onResetComplete() }
                    showResetDialog = false
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            profile?.let { p ->
                ProfileSection(profile = p, onSave = viewModel::updateProfile)
            }
            schedule?.let { s ->
                MealTimesSection(schedule = s, onSave = viewModel::updateSchedule)
            }
            BatteryOptimizationCard()
            // Reset section
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Danger Zone", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reset All Data")
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationCard() {
    val context = LocalContext.current
    val pm = context.getSystemService(PowerManager::class.java)
    val isExempt = pm.isIgnoringBatteryOptimizations(context.packageName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExempt) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (isExempt) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = if (isExempt) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error
                )
                Text(
                    "Battery Optimization",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isExempt) {
                Text(
                    "Unrestricted — alarms will fire reliably.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    "Battery optimization is ON. On OnePlus / Xiaomi / Samsung devices this silently kills alarms. Tap below to disable it for this app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:${context.packageName}"))
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Disable Battery Optimization") }
            }
        }
    }
}

@Composable
private fun ProfileSection(profile: UserProfile, onSave: (UserProfile) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var name by remember(profile) { mutableStateOf(profile.name) }
    var age by remember(profile) { mutableStateOf(profile.age.toString()) }
    var weight by remember(profile) { mutableStateOf(profile.weightKg.toString()) }
    var height by remember(profile) { mutableStateOf(profile.heightCm.toString()) }
    var goal by remember(profile) { mutableStateOf(profile.goal) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Expand")
                }
            }
            if (!expanded) {
                Text("${profile.name}, ${profile.age}y  •  ${profile.weightKg}kg  •  ${profile.heightCm}cm",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Goal: ${profile.goal.name.replace("_"," ")}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary)
            } else {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = age, onValueChange = { age = it },
                    label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = weight, onValueChange = { weight = it },
                    label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = height, onValueChange = { height = it },
                    label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                Text("Goal", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Goal.entries.forEach { g ->
                        FilterChip(selected = g == goal, onClick = { goal = g },
                            label = { Text(g.name.replace("_", " ")) })
                    }
                }
                Button(
                    onClick = {
                        onSave(profile.copy(name = name, age = age.toIntOrNull() ?: profile.age,
                            weightKg = weight.toFloatOrNull() ?: profile.weightKg,
                            heightCm = height.toFloatOrNull() ?: profile.heightCm, goal = goal))
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Profile") }
            }
        }
    }
}

@Composable
private fun MealTimesSection(schedule: MealSchedule, onSave: (MealSchedule) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var breakfast by remember(schedule) { mutableStateOf(schedule.breakfastTime) }
    var lunch by remember(schedule) { mutableStateOf(schedule.lunchTime) }
    var dinner by remember(schedule) { mutableStateOf(schedule.dinnerTime) }
    var snacks by remember(schedule) { mutableStateOf(schedule.snacksTime) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Meal Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Expand")
                }
            }
            if (!expanded) {
                listOf("Breakfast" to schedule.breakfastTime, "Lunch" to schedule.lunchTime,
                    "Dinner" to schedule.dinnerTime, "Snacks" to schedule.snacksTime).forEach { (label, time) ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Text(com.dietreminder.util.TimeUtils.displayTime(time),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                TimePickerRow("Breakfast", breakfast, { breakfast = it })
                TimePickerRow("Lunch", lunch, { lunch = it })
                TimePickerRow("Dinner", dinner, { dinner = it })
                TimePickerRow("Snacks", snacks, { snacks = it })
                Button(
                    onClick = {
                        onSave(MealSchedule(breakfastTime = breakfast, lunchTime = lunch,
                            dinnerTime = dinner, snacksTime = snacks))
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Meal Times") }
            }
        }
    }
}
