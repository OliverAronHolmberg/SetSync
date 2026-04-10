package com.example.setsync.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.setsync.model.Exercise
import com.example.setsync.model.PersonalBest
import com.example.setsync.model.WorkoutSession
import com.example.setsync.model.WorkoutSet

@Database(entities = [Exercise::class, WorkoutSession::class, WorkoutSet::class, PersonalBest::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}