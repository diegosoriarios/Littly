package com.diego.littly.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val iconKey: String = "sparkles",
    val createdAtEpochDay: Long,
    val archivedAtEpochDay: Long? = null,
    val goalDaysBitmask: Int,
    val reminderEnabled: Boolean = false,
    val reminderMinuteOfDay: Int? = null,
    val reminderDaysBitmask: Int? = null,
    val colorIndex: Int = 0,
    val sortOrder: Int = 0
)
