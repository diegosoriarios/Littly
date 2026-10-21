package com.diego.littly.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LittlyMigrationTest {

    @Test
    fun migrate1To2_preservesHabitsAndAddsDefaults() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(TEST_DB)
        createVersion1Database(context)

        val database = Room.databaseBuilder(context, LittlyDatabase::class.java, TEST_DB)
            .addMigrations(LittlyDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        val migrated = database.habitDao().getById(1)
        assertEquals("draw", migrated?.name)
        assertEquals("sparkles", migrated?.iconKey)
        assertNull(migrated?.description)
        assertEquals(127, migrated?.goalDaysBitmask)
        assertEquals(2, migrated?.colorIndex)
        database.close()
        context.deleteDatabase(TEST_DB)
    }

    private fun createVersion1Database(context: Context) {
        val db = context.openOrCreateDatabase(TEST_DB, Context.MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habits` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`createdAtEpochDay` INTEGER NOT NULL, " +
                "`archivedAtEpochDay` INTEGER, " +
                "`goalDaysBitmask` INTEGER NOT NULL, " +
                "`reminderEnabled` INTEGER NOT NULL, " +
                "`reminderMinuteOfDay` INTEGER, " +
                "`reminderDaysBitmask` INTEGER, " +
                "`colorIndex` INTEGER NOT NULL, " +
                "`sortOrder` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `habit_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`habitId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, " +
                "`completedAtMillis` INTEGER NOT NULL, " +
                "`source` TEXT NOT NULL, " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_logs_habitId_epochDay` " +
                "ON `habit_logs` (`habitId`, `epochDay`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `ideas` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`habitId` INTEGER NOT NULL, " +
                "`title` TEXT, " +
                "`text` TEXT NOT NULL, " +
                "`category` TEXT NOT NULL, " +
                "`createdAtMillis` INTEGER NOT NULL, " +
                "`scheduledForEpochDay` INTEGER, " +
                "`status` TEXT NOT NULL, " +
                "`model` TEXT NOT NULL, " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ideas_habitId` ON `ideas` (`habitId`)")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_ideas_habitId_scheduledForEpochDay` " +
                "ON `ideas` (`habitId`, `scheduledForEpochDay`)"
        )
        db.execSQL(
            "INSERT INTO habits (name, createdAtEpochDay, archivedAtEpochDay, goalDaysBitmask, " +
                "reminderEnabled, reminderMinuteOfDay, reminderDaysBitmask, colorIndex, sortOrder) " +
                "VALUES ('draw', 20000, NULL, 127, 0, NULL, NULL, 2, 0)"
        )
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '3cc9ecdda9400f28d899dd4a7e9f75a0')")
        db.version = 1
        db.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
