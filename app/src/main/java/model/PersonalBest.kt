package com.example.setsync.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_bests")
data class PersonalBest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val date: String,
    val weight: Double,
    val reps: Int
)