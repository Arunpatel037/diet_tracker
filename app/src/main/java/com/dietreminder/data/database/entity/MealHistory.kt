package com.dietreminder.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACKS }

@Entity(tableName = "meal_history")
data class MealHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "yyyy-MM-dd"
    val mealType: MealType,
    val completedAt: Long,      // epoch millis
    val photoPath: String?
)
