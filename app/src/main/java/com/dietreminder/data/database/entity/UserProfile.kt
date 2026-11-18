package com.dietreminder.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Goal { WEIGHT_LOSS, WEIGHT_GAIN, MAINTENANCE }

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Float,
    val goal: Goal
)
