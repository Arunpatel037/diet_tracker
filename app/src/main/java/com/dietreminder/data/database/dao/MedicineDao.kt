package com.dietreminder.data.database.dao

import androidx.room.*
import com.dietreminder.data.database.entity.Medicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicine ORDER BY name ASC")
    fun observeAll(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicine WHERE isActive = 1 ORDER BY reminderTime ASC")
    suspend fun getActive(): List<Medicine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(medicine: Medicine): Long

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("UPDATE medicine SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
