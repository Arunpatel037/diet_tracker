package com.dietreminder.data.database

import androidx.room.TypeConverter
import com.dietreminder.data.database.entity.Goal
import com.dietreminder.data.database.entity.MealType

class Converters {
    @TypeConverter fun fromGoal(v: Goal): String = v.name
    @TypeConverter fun toGoal(v: String): Goal = Goal.valueOf(v)
    @TypeConverter fun fromMealType(v: MealType): String = v.name
    @TypeConverter fun toMealType(v: String): MealType = MealType.valueOf(v)
}
