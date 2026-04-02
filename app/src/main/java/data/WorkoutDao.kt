package com.example.setsync.data

import androidx.room.*
import com.example.setsync.model.Exercise
import com.example.setsync.model.WorkoutSession
import com.example.setsync.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    fun getAllSessionsFlow(): Flow<List<WorkoutSession>>

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Int)

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

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId")
    fun getSetsForSessionFlow(sessionId: Int): Flow<List<WorkoutSet>>
}