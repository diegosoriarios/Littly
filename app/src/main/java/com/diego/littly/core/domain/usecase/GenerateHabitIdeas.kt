package com.diego.littly.core.domain.usecase

import com.diego.littly.core.domain.model.HabitCategory
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.llm.HabitClassifier
import com.diego.littly.core.llm.IdeaEvent
import com.diego.littly.core.llm.IdeaGenerator
import com.diego.littly.core.llm.IdeaRequest
import com.diego.littly.widget.WidgetUpdateHelper
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GenerateHabitIdeas @Inject constructor(
    private val habitRepository: HabitRepository,
    private val ideaRepository: IdeaRepository,
    private val ideaGenerator: IdeaGenerator,
    private val widgetUpdateHelper: WidgetUpdateHelper
) {

    operator fun invoke(habitId: Long): Flow<IdeaEvent> = flow {
        val habit = habitRepository.getHabit(habitId)
        if (habit == null) {
            emit(IdeaEvent.Failed("habit not found"))
            return@flow
        }
        val category = HabitClassifier.classify(habit.name)
        val recentIdeas = ideaRepository.getRecentTexts(habitId, RECENT_IDEA_LIMIT)
        val request = IdeaRequest(
            habitName = habit.name,
            category = category,
            recentIdeas = recentIdeas,
            count = BATCH_SIZE
        )
        ideaGenerator.generate(request).collect { event ->
            when (event) {
                is IdeaEvent.Completed -> {
                    ideaRepository.saveGenerated(habitId, category, event.model, event.ideas)
                    widgetUpdateHelper.updateAllWidgets()
                    emit(event)
                }
                else -> emit(event)
            }
        }
    }

    companion object {
        const val RECENT_IDEA_LIMIT = 10
        const val BATCH_SIZE = 3
    }
}
