package com.dietreminder.data.repository

import com.dietreminder.data.database.dao.MedicineDao
import com.dietreminder.data.database.dao.MedicineHistoryDao
import com.dietreminder.data.database.entity.Medicine
import com.dietreminder.data.database.entity.MedicineHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor(
    private val medicineDao: MedicineDao,
    private val historyDao: MedicineHistoryDao
) {
    fun observeAll(): Flow<List<Medicine>> = medicineDao.observeAll()
    suspend fun getActive(): List<Medicine> = medicineDao.getActive()
    suspend fun save(medicine: Medicine): Long = medicineDao.upsert(medicine)
    suspend fun delete(medicine: Medicine) = medicineDao.delete(medicine)
    suspend fun setActive(id: Long, active: Boolean) = medicineDao.setActive(id, active)

    fun observeHistoryByDate(date: String): Flow<List<MedicineHistory>> =
        historyDao.observeByDate(date)
    suspend fun recordTaken(history: MedicineHistory): Long = historyDao.insert(history)
    suspend fun cleanOldHistory(cutoff: String) = historyDao.deleteOlderThan(cutoff)
}
