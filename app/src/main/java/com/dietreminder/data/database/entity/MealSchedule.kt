package com.dietreminder.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_schedule")
data class MealSchedule(
    @PrimaryKey val id: Int = 1,
    val breakfastTime: String,  // "HH:mm"
    val lunchTime: String,
    val dinnerTime: String,
    val snacksTime: String
)
