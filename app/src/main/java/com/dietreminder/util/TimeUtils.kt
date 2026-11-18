package com.dietreminder.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    // Emits the current date string immediately, then re-emits at each midnight.
    // Use with flatMapLatest so date-dependent queries automatically refresh after midnight.
    fun currentDateFlow(): Flow<String> = flow {
        while (true) {
            emit(today())
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            delay(cal.timeInMillis - System.currentTimeMillis())
        }
    }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

    fun cutoffDate(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    fun parseHHmm(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
    }

    fun formatHHmm(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour, minute)

    fun displayTime(time: String): String {
        return try {
            val (h, m) = parseHHmm(time)
            val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
            displayTimeFormat.format(cal.time)
        } catch (e: Exception) { time }
    }

    fun nextAlarmMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    fun tomorrowAlarmMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
