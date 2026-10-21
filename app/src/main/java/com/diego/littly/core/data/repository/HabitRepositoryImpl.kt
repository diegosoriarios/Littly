package com.diego.littly.core.data.repository

import com.diego.littly.core.data.mapper.toDomain
import com.diego.littly.core.data.mapper.toEntity
import com.diego.littly.core.database.dao.HabitDao
import com.diego.littly.core.database.dao.HabitLogDao
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.widget.WidgetUpdateHelper
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val timeProvider: TimeProvider,
    private val widgetUpdateHelper: WidgetUpdateHelper
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        habitDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeArchivedHabits(): Flow<List<Habit>> =
        habitDao.observeArchived().map { list -> list.map { it.toDomain() } }

    override fun observeHabit(habitId: Long): Flow<Habit?> =
        habitDao.observeById(habitId).map { it?.toDomain() }

    override suspend fun getHabit(habitId: Long): Habit? =
        habitDao.getById(habitId)?.toDomain()

    override suspend fun getActiveHabits(): List<Habit> =
        habitDao.getActive().map { it.toDomain() }

    override suspend fun addHabit(
        name: String,
        goalDays: Set<DayOfWeek>,
        reminder: Reminder,
        colorIndex: Int,
        description: String?,
        iconKey: String
    ): Long {
        val sortOrder = (habitDao.maxSortOrder() ?: -1) + 1
        val id = habitDao.insert(
            Habit(
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                iconKey = iconKey,
                createdAt = timeProvider.today(),
                goalDays = goalDays,
                reminder = reminder,
                colorIndex = colorIndex,
                sortOrder = sortOrder
            ).toEntity()
        )
        widgetUpdateHelper.updateAllWidgets()
        return id
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit.toEntity())
        widgetUpdateHelper.updateAllWidgets()
    }

    override suspend fun setArchived(habitId: Long, archived: Boolean) {
        habitDao.setArchived(
            id = habitId,
            epochDay = if (archived) timeProvider.today().toEpochDay() else null
        )
        widgetUpdateHelper.updateAllWidgets()
    }

    override fun observeLogs(habitId: Long): Flow<List<HabitLog>> =
        habitLogDao.observeForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override fun observeAllLogs(): Flow<List<HabitLog>> =
        habitLogDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun toggleCompletion(habitId: Long, date: LocalDate, source: CompletionSource) {
        val epochDay = date.toEpochDay()
        val existing = habitLogDao.getForDay(habitId, epochDay)
        if (existing == null) {
            habitLogDao.insert(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    completedAtMillis = timeProvider.nowMillis(),
                    source = source
                ).toEntity()
            )
        } else {
            habitLogDao.deleteForDay(habitId, epochDay)
        }
        widgetUpdateHelper.updateAllWidgets()
    }
}
