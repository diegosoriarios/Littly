package com.diego.littly.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diego.littly.core.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY epochDay DESC")
    fun observeForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY epochDay DESC")
    fun observeAll(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay LIMIT 1")
    suspend fun getForDay(habitId: Long, epochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteForDay(habitId: Long, epochDay: Long)

    @Query("SELECT * FROM habit_logs WHERE epochDay = :epochDay")
    suspend fun getAllForDay(epochDay: Long): List<HabitLogEntity>

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId")
    suspend fun countForHabit(habitId: Long): Int
}
