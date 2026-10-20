package com.diego.littly.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diego.littly.core.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE archivedAtEpochDay IS NULL ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE archivedAtEpochDay IS NULL ORDER BY sortOrder ASC, id ASC")
    suspend fun getActive(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE archivedAtEpochDay IS NOT NULL ORDER BY archivedAtEpochDay DESC")
    fun observeArchived(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET archivedAtEpochDay = :epochDay WHERE id = :id")
    suspend fun setArchived(id: Long, epochDay: Long?)

    @Query("SELECT MAX(sortOrder) FROM habits")
    suspend fun maxSortOrder(): Int?
}
