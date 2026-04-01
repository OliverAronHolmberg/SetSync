package com.example.setsync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.setsync.model.Exercise
import com.example.setsync.model.WorkoutSession
import com.example.setsync.model.WorkoutSet

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long


    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)


    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>


    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: Int): List<WorkoutSet>
}