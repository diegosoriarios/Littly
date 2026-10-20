package com.diego.littly.feature.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.streak.StreakCalculator
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.GenerateHabitIdeas
import com.diego.littly.core.llm.IdeaEvent
import com.diego.littly.core.notifications.ReminderPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitDetailState(
    val isLoading: Boolean = true,
    val habit: Habit? = null,
    val completedDays: Set<LocalDate> = emptySet(),
    val today: LocalDate = LocalDate.now(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletions: Int = 0
) {
    val completedToday: Boolean
        get() = today in completedDays
}

data class IdeaStreamState(
    val isGenerating: Boolean = false,
    val streamText: String = "",
    val latestIdea: Idea? = null,
    val recentIdeas: List<Idea> = emptyList(),
    val error: Boolean = false
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val ideaRepository: IdeaRepository,
    private val generateHabitIdeas: GenerateHabitIdeas,
    private val timeProvider: TimeProvider,
    private val reminderPlanner: ReminderPlanner
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle["habitId"])
    private val autoGenerate: Boolean = savedStateHandle["generateIdea"] ?: false

    init {
        if (autoGenerate) {
            viewModelScope.launch {
                state.first { !it.isLoading && it.habit != null }
                generateIdea()
            }
        }
    }

    val state: StateFlow<HabitDetailState> =
        combine(
            habitRepository.observeHabit(habitId),
            habitRepository.observeLogs(habitId)
        ) { habit, logs ->
            if (habit == null) {
                HabitDetailState(isLoading = false)
            } else {
                val today = timeProvider.today()
                val completedDays = logs.mapTo(mutableSetOf()) { it.date }
                HabitDetailState(
                    isLoading = false,
                    habit = habit,
                    completedDays = completedDays,
                    today = today,
                    currentStreak = StreakCalculator.currentStreak(
                        completedDays = completedDays,
                        goalDays = habit.goalDays,
                        today = today,
                        createdAt = habit.createdAt
                    ),
                    longestStreak = StreakCalculator.longestStreak(
                        completedDays = completedDays,
                        goalDays = habit.goalDays,
                        today = today,
                        createdAt = habit.createdAt
                    ),
                    totalCompletions = completedDays.size
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitDetailState()
        )

    private val generation = MutableStateFlow(IdeaStreamState())

    val ideaState: StateFlow<IdeaStreamState> =
        combine(
            generation,
            ideaRepository.observeForHabit(habitId, IDEA_HISTORY_LIMIT)
        ) { generating, ideas ->
            generating.copy(latestIdea = ideas.firstOrNull(), recentIdeas = ideas)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IdeaStreamState()
        )

    fun generateIdea() {
        if (generation.value.isGenerating) return
        viewModelScope.launch {
            generation.update {
                it.copy(isGenerating = true, streamText = "", error = false)
            }
            generateHabitIdeas(habitId).collect { event ->
                when (event) {
                    is IdeaEvent.Partial -> generation.update {
                        it.copy(streamText = event.textSoFar)
                    }
                    is IdeaEvent.Completed -> generation.update {
                        it.copy(isGenerating = false, streamText = "", error = false)
                    }
                    is IdeaEvent.Failed -> generation.update {
                        it.copy(isGenerating = false, error = true)
                    }
                }
            }
        }
    }

    fun toggleToday() {
        viewModelScope.launch {
            habitRepository.toggleCompletion(
                habitId,
                timeProvider.today(),
                CompletionSource.APP
            )
        }
    }

    fun setArchived(archived: Boolean) {
        viewModelScope.launch {
            habitRepository.setArchived(habitId, archived)
            if (archived) {
                reminderPlanner.cancelHabit(habitId)
            } else {
                reminderPlanner.rescheduleHabit(habitId)
            }
        }
    }

    private companion object {
        const val IDEA_HISTORY_LIMIT = 8
    }
}
