package com.diego.littly.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diego.littly.core.database.dao.HabitDao
import com.diego.littly.core.database.dao.HabitLogDao
import com.diego.littly.core.database.dao.IdeaDao
import com.diego.littly.core.database.entity.HabitEntity
import com.diego.littly.core.database.entity.HabitLogEntity
import com.diego.littly.core.database.entity.IdeaEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, IdeaEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LittlyDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun ideaDao(): IdeaDao

    companion object {
        const val NAME = "littly.db"
    }
}
