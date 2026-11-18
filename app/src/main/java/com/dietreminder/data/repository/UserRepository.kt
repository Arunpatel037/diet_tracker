package com.dietreminder.data.repository

import com.dietreminder.data.database.dao.MealScheduleDao
import com.dietreminder.data.database.dao.UserProfileDao
import com.dietreminder.data.database.entity.MealSchedule
import com.dietreminder.data.database.entity.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val mealScheduleDao: MealScheduleDao
) {
    fun observeProfile(): Flow<UserProfile?> = userProfileDao.observe()
    suspend fun getProfile(): UserProfile? = userProfileDao.get()
    suspend fun saveProfile(profile: UserProfile) = userProfileDao.upsert(profile)

    fun observeSchedule(): Flow<MealSchedule?> = mealScheduleDao.observe()
    suspend fun getSchedule(): MealSchedule? = mealScheduleDao.get()
    suspend fun saveSchedule(schedule: MealSchedule) = mealScheduleDao.upsert(schedule)
}
