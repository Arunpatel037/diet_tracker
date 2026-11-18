package com.dietreminder.data.repository

import com.dietreminder.data.database.dao.MealHistoryDao
import com.dietreminder.data.database.entity.MealHistory
import com.dietreminder.data.database.entity.MealType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepository @Inject constructor(private val dao: MealHistoryDao) {

    fun observeByDate(date: String): Flow<List<MealHistory>> = dao.observeByDate(date)

    fun observeRange(from: String, to: String): Flow<List<MealHistory>> = dao.observeRange(from, to)

    suspend fun getByDateAndType(date: String, type: MealType): MealHistory? =
        dao.getByDateAndType(date, type)

    suspend fun saveMealCompletion(history: MealHistory): Long = dao.insert(history)

    suspend fun deleteOlderThan(cutoff: String) = dao.deleteOlderThan(cutoff)

    suspend fun getRecentDates(): List<String> = dao.getRecentDates()
}
