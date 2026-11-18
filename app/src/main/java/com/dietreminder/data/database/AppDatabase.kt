package com.dietreminder.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dietreminder.data.database.dao.*
import com.dietreminder.data.database.entity.*

@Database(
    entities = [UserProfile::class, MealSchedule::class, MealHistory::class, Medicine::class, MedicineHistory::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun mealScheduleDao(): MealScheduleDao
    abstract fun mealHistoryDao(): MealHistoryDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineHistoryDao(): MedicineHistoryDao
}
