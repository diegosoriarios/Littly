package com.diego.littly.core.data.repository

import com.diego.littly.core.data.mapper.toDomain
import com.diego.littly.core.database.dao.IdeaDao
import com.diego.littly.core.database.entity.IdeaEntity
import com.diego.littly.core.domain.model.GeneratedIdea
import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.model.IdeaStatus
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.time.TimeProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IdeaRepositoryImpl @Inject constructor(
    private val ideaDao: IdeaDao,
    private val timeProvider: TimeProvider
) : IdeaRepository {

    override fun observeForHabit(habitId: Long, limit: Int): Flow<List<Idea>> =
        ideaDao.observeForHabit(habitId, limit).map { list -> list.map { it.toDomain() } }

    override fun observeLatest(limit: Int): Flow<List<Idea>> =
        ideaDao.observeLatest(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getRecentIdeas(habitId: Long, limit: Int): List<Idea> =
        ideaDao.getRecentForHabit(habitId, limit).map { it.toDomain() }

    override suspend fun getRecentTexts(habitId: Long, limit: Int): List<String> =
        getRecentIdeas(habitId, limit).map { it.text }

    override suspend fun saveGenerated(
        habitId: Long,
        category: HabitCategory,
        model: String,
        ideas: List<GeneratedIdea>
    ): List<Long> = ideaDao.insertAll(
        ideas.map { generated ->
            IdeaEntity(
                habitId = habitId,
                title = generated.title,
                text = generated.text,
                category = category,
                createdAtMillis = timeProvider.nowMillis(),
                scheduledForEpochDay = null,
                status = IdeaStatus.SPARE,
                model = model
            )
        }
    )

    override suspend fun getQueuedIdeaForDate(habitId: Long, date: LocalDate): Idea? =
        ideaDao.getQueuedForDay(habitId, date.toEpochDay())?.toDomain()

    override suspend fun scheduleIdea(ideaId: Long, date: LocalDate) {
        ideaDao.schedule(ideaId, date.toEpochDay())
    }

    override suspend fun markShown(ideaId: Long) = ideaDao.markShown(ideaId)

    override suspend fun markUsed(ideaId: Long) = ideaDao.markUsed(ideaId)

    override suspend fun countScheduledDays(habitId: Long, from: LocalDate, to: LocalDate): Int =
        ideaDao.countScheduledDays(habitId, from.toEpochDay(), to.toEpochDay())

    override suspend fun deleteForHabit(habitId: Long) = ideaDao.deleteForHabit(habitId)
}
