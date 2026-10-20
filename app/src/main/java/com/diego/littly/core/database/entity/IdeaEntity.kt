package com.diego.littly.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.model.IdeaStatus

@Entity(
    tableName = "ideas",
    indices = [
        Index("habitId"),
        Index(value = ["habitId", "scheduledForEpochDay"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val title: String?,
    val text: String,
    val category: HabitCategory,
    val createdAtMillis: Long,
    val scheduledForEpochDay: Long? = null,
    val status: IdeaStatus,
    val model: String
)
