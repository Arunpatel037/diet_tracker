package com.dietreminder.data.database.dao

import androidx.room.*
import com.dietreminder.data.database.entity.MealSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface MealScheduleDao {
    @Query("SELECT * FROM meal_schedule WHERE id = 1 LIMIT 1")
    fun observe(): Flow<MealSchedule?>

    @Query("SELECT * FROM meal_schedule WHERE id = 1 LIMIT 1")
    suspend fun get(): MealSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: MealSchedule)
}
