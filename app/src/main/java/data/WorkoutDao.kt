package com.example.setsync.data

import androidx.room.*
import com.example.setsync.model.Exercise
import com.example.setsync.model.WorkoutSession
import com.example.setsync.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    // Flow allows the UI to update automatically when data changes
    @Query("SELECT * FROM exercises")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSetsForSession(sessionId: Int): List<WorkoutSet>
}