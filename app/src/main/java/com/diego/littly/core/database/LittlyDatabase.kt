package com.diego.littly.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diego.littly.core.database.dao.HabitDao
import com.diego.littly.core.database.dao.HabitLogDao
import com.diego.littly.core.database.dao.IdeaDao
import com.diego.littly.core.database.entity.HabitEntity
import com.diego.littly.core.database.entity.HabitLogEntity
import com.diego.littly.core.database.entity.IdeaEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, IdeaEntity::class],
    version = 2,
    exportSchema = true
)
abstract class LittlyDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun ideaDao(): IdeaDao

    companion object {
        const val NAME = "littly.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN description TEXT")
                db.execSQL(
                    "ALTER TABLE habits ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'sparkles'"
                )
            }
        }
    }
}
