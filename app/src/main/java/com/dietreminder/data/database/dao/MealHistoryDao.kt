package com.dietreminder.data.database.dao

import androidx.room.*
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.database.entity.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface MealHistoryDao {
    @Query("SELECT * FROM meal_history WHERE date = :date ORDER BY completedAt ASC")
    fun observeByDate(date: String): Flow<List<MealHistory>>

    @Query("SELECT * FROM meal_history WHERE date BETWEEN :from AND :to ORDER BY date ASC, completedAt ASC")
    fun observeRange(from: String, to: String): Flow<List<MealHistory>>

    @Query("SELECT * FROM meal_history WHERE date = :date AND mealType = :type LIMIT 1")
    suspend fun getByDateAndType(date: String, type: MealType): MealHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: MealHistory): Long

    @Query("DELETE FROM meal_history WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)

    @Query("SELECT DISTINCT date FROM meal_history ORDER BY date DESC LIMIT 30")
    suspend fun getRecentDates(): List<String>
}
