package com.dietreminder.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.database.entity.MealType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dayMeals by viewModel.selectedDayMeals.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { viewModel.cleanOldData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { showCalendar = !showCalendar }) {
                        Icon(
                            if (showCalendar) Icons.AutoMirrored.Filled.ViewList else Icons.Default.CalendarMonth,
                            contentDescription = "Toggle view"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (showCalendar) {
                CalendarView(
                    history = history,
                    selectedDate = selectedDate,
                    onDateSelected = viewModel::selectDate
                )
            } else {
                // List of all dates with history
                val dates = history.map { it.date }.distinct().sortedDescending()
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dates, key = { it }) { date ->
                        val mealsOnDate = history.filter { it.date == date }
                        DateHistoryCard(
                            date = date,
                            meals = mealsOnDate,
                            isSelected = date == selectedDate,
                            onClick = { viewModel.selectDate(date); showCalendar = true }
                        )
                    }
                }
                return@Column
            }

            HorizontalDivider()
            Text(
                "Meals on $selectedDate",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (dayMeals.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No meals recorded", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayMeals, key = { it.id }) { meal -> MealHistoryCard(meal = meal) }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    history: List<MealHistory>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val datesWithData = history.map { it.date }.toSet()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Month navigation
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, "Previous month")
            }
            Text(
                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { if (currentMonth < YearMonth.now()) currentMonth = currentMonth.plusMonths(1) },
                enabled = currentMonth < YearMonth.now()
            ) {
                Icon(Icons.Default.ChevronRight, "Next month")
            }
        }

        // Day of week headers
        Row(Modifier.fillMaxWidth()) {
            listOf("S","M","T","W","T","F","S").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Calendar days
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break
            Row(Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayNum = week * 7 + col - firstDayOfWeek + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val date = currentMonth.atDay(dayNum)
                        val dateStr = date.format(formatter)
                        val hasData = dateStr in datesWithData
                        val isSelected = dateStr == selectedDate
                        val isFuture = date.isAfter(today)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        hasData -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable(enabled = !isFuture) { onDateSelected(dateStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$dayNum",
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun MealHistoryCard(meal: MealHistory) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (meal.photoPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(meal.photoPath).crossfade(true).build(),
                    contentDescription = "Meal photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
                )
            } else {
                Box(
                    Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(meal.mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val timeFormatter = remember {
                    DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault())
                }
                val time = remember(meal.completedAt) {
                    timeFormatter.format(Instant.ofEpochMilli(meal.completedAt))
                }
                Text("Completed at $time", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DateHistoryCard(date: String, meals: List<MealHistory>, isSelected: Boolean, onClick: () -> Unit) {
    val completedCount = meals.size
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("$completedCount meal(s) completed", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                MealType.entries.forEach { type ->
                    val completed = meals.any { it.mealType == type }
                    Icon(
                        if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = type.name,
                        tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
