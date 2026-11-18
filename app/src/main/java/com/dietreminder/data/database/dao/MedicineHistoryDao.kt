package com.dietreminder.data.database.dao

import androidx.room.*
import com.dietreminder.data.database.entity.MedicineHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineHistoryDao {
    @Query("SELECT * FROM medicine_history WHERE date = :date ORDER BY takenAt ASC")
    fun observeByDate(date: String): Flow<List<MedicineHistory>>

    @Insert
    suspend fun insert(history: MedicineHistory): Long

    @Query("DELETE FROM medicine_history WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}
