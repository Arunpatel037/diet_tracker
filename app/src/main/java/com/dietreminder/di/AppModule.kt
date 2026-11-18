package com.dietreminder.di

import android.content.Context
import androidx.room.Room
import com.dietreminder.data.database.AppDatabase
import com.dietreminder.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "diet_reminder.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideMealScheduleDao(db: AppDatabase): MealScheduleDao = db.mealScheduleDao()
    @Provides fun provideMealHistoryDao(db: AppDatabase): MealHistoryDao = db.mealHistoryDao()
    @Provides fun provideMedicineDao(db: AppDatabase): MedicineDao = db.medicineDao()
    @Provides fun provideMedicineHistoryDao(db: AppDatabase): MedicineHistoryDao = db.medicineHistoryDao()
}
