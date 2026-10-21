package com.diego.littly.core.domain.usecase

import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.llm.HabitClassifier
import com.diego.littly.core.llm.IdeaEvent
import com.diego.littly.core.llm.IdeaGenerator
import com.diego.littly.core.llm.IdeaRequest
import com.diego.littly.core.notifications.ReminderTime
import com.diego.littly.widget.WidgetUpdateHelper
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IdeaQueueFillerImpl @Inject constructor(
    private val habitRepository: HabitRepository,
    private val ideaRepository: IdeaRepository,
    private val ideaGenerator: IdeaGenerator,
    private val timeProvider: TimeProvider,
    private val widgetUpdateHelper: WidgetUpdateHelper,
    private val settingsRepository: SettingsRepository
) : IdeaQueueFiller {
    override suspend fun fillQueue(habitId: Long, days: Int) {
        if (!settingsRepository.ideaEngineEnabled.first()) return
        val habit = habitRepository.getHabit(habitId) ?: return
        if (!habit.reminder.enabled) return
        
        val today = timeProvider.today()
        
        // Find which days in [today, today+days-1] are goal days and don't have a queued idea
        val unscheduledDays = mutableListOf<LocalDate>()
        for (i in 0 until days) {
            val date = today.plusDays(i.toLong())
            if (date.dayOfWeek in ReminderTime.effectiveReminderDays(habit)) {
                val existing = ideaRepository.getQueuedIdeaForDate(habitId, date)
                if (existing == null) {
                    unscheduledDays.add(date)
                }
            }
        }
        
        if (unscheduledDays.isEmpty()) return
        
        val category = HabitClassifier.classify(habit.name)
        val recentTexts = ideaRepository.getRecentTexts(habitId, 10)
        
        val request = IdeaRequest(
            habitName = habit.name,
            category = category,
            recentIdeas = recentTexts,
            count = unscheduledDays.size
        )
        
        var generatedIdeas = emptyList<com.diego.littly.core.domain.model.GeneratedIdea>()
        var modelTag = "unknown"
        
        ideaGenerator.generate(request).collect { event ->
            when (event) {
                is IdeaEvent.Completed -> {
                    generatedIdeas = event.ideas
                    modelTag = event.model
                }
                else -> {} // ignore partials and failures
            }
        }
        
        if (generatedIdeas.isEmpty()) return
        
        // Save ideas and schedule them
        val toSave = generatedIdeas.take(unscheduledDays.size)
        val ids = ideaRepository.saveGenerated(habitId, category, modelTag, toSave)
        
        ids.forEachIndexed { index, id ->
            if (index < unscheduledDays.size) {
                ideaRepository.scheduleIdea(id, unscheduledDays[index])
            }
        }
        widgetUpdateHelper.updateAllWidgets()
    }
    
    override suspend fun fillAllQueues(days: Int) {
        val habits = habitRepository.getActiveHabits()
        habits.filter { it.reminder.enabled }.forEach { fillQueue(it.id, days) }
    }
}
