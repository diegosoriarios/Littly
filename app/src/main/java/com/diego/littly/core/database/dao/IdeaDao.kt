package com.diego.littly.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diego.littly.core.database.entity.IdeaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {

    @Insert
    suspend fun insertAll(ideas: List<IdeaEntity>): List<Long>

    @Query("SELECT * FROM ideas WHERE habitId = :habitId ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeForHabit(habitId: Long, limit: Int): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE habitId = :habitId ORDER BY createdAtMillis DESC LIMIT :limit")
    suspend fun getRecentForHabit(habitId: Long, limit: Int): List<IdeaEntity>

    @Query(
        "SELECT * FROM ideas WHERE habitId = :habitId AND status = 'QUEUED' " +
            "AND scheduledForEpochDay = :epochDay ORDER BY id ASC LIMIT 1"
    )
    suspend fun getQueuedForDay(habitId: Long, epochDay: Long): IdeaEntity?

    @Query("UPDATE ideas SET scheduledForEpochDay = :epochDay, status = 'QUEUED' WHERE id = :id")
    suspend fun schedule(id: Long, epochDay: Long)

    @Query("UPDATE ideas SET status = 'SHOWN' WHERE id = :id")
    suspend fun markShown(id: Long)

    @Query("UPDATE ideas SET status = 'USED' WHERE id = :id")
    suspend fun markUsed(id: Long)

    @Query(
        "SELECT COUNT(DISTINCT scheduledForEpochDay) FROM ideas " +
            "WHERE habitId = :habitId AND scheduledForEpochDay BETWEEN :from AND :to"
    )
    suspend fun countScheduledDays(habitId: Long, from: Long, to: Long): Int

    @Query("DELETE FROM ideas WHERE habitId = :habitId")
    suspend fun deleteForHabit(habitId: Long)
}
