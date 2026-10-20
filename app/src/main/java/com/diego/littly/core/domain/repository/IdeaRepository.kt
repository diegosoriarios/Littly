package com.diego.littly.core.domain.repository

import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.model.Idea
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface IdeaRepository {
    fun observeForHabit(habitId: Long, limit: Int = 50): Flow<List<Idea>>
    suspend fun getRecentIdeas(habitId: Long, limit: Int = 10): List<Idea>
    suspend fun getRecentTexts(habitId: Long, limit: Int = 10): List<String>
    suspend fun saveGenerated(
        habitId: Long,
        category: HabitCategory,
        model: String,
        ideas: List<GeneratedIdea>
    ): List<Long>

    suspend fun getQueuedIdeaForDate(habitId: Long, date: LocalDate): Idea?
    suspend fun scheduleIdea(ideaId: Long, date: LocalDate)
    suspend fun markShown(ideaId: Long)
    suspend fun markUsed(ideaId: Long)
    suspend fun countScheduledDays(habitId: Long, from: LocalDate, to: LocalDate): Int
    suspend fun deleteForHabit(habitId: Long)
}
