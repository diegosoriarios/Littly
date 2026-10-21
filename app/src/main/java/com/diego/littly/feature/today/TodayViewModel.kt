package com.diego.littly.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.model.Weekdays
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.repository.IdeaRepository
import com.diego.littly.core.domain.repository.SettingsRepository
import com.diego.littly.core.domain.stats.TodayOverview
import com.diego.littly.core.domain.stats.StatsCalculator
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.ObserveTodayHabits
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodayGreeting { MORNING, AFTERNOON, EVENING }

data class TodayIdea(
    val habitId: Long,
    val habitName: String,
    val title: String?,
    val text: String,
    val model: String,
    val earlierText: String?
)

data class TodayState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val greeting: TodayGreeting = TodayGreeting.MORNING,
    val displayName: String = "",
    val dueHabits: List<HabitWithProgress> = emptyList(),
    val allActiveHabits: List<HabitWithProgress> = emptyList(),
    val overview: TodayOverview? = null,
    val completedDaysByHabit: Map<Long, Set<LocalDate>> = emptyMap(),
    val idea: TodayIdea? = null,
    val ideaEngineEnabled: Boolean = true
) {
    val completedCount: Int
        get() = dueHabits.count { it.completedToday }

    val allDueDone: Boolean
        get() = dueHabits.isNotEmpty() && completedCount == dueHabits.size
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    observeTodayHabits: ObserveTodayHabits,
    private val habitRepository: HabitRepository,
    private val ideaRepository: IdeaRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    val state: StateFlow<TodayState> =
        reload.flatMapLatest {
            combine(
                observeTodayHabits(),
                habitRepository.observeAllLogs(),
                settingsRepository.displayName,
                settingsRepository.ideaEngineEnabled,
                ideaRepository.observeLatest(24)
            ) { progress, logs, name, ideaEnabled, ideas ->
                buildState(progress, logs, name, ideaEnabled, ideas)
            }.catch {
                emit(TodayState(isLoading = false, hasError = true))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayState()
        )

    fun retry() {
        reload.value = reload.value + 1
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch {
            habitRepository.toggleCompletion(
                habitId = habitId,
                date = timeProvider.today(),
                source = CompletionSource.APP
            )
        }
    }

    private fun buildState(
        progress: List<HabitWithProgress>,
        logs: List<HabitLog>,
        name: String,
        ideaEnabled: Boolean,
        ideas: List<Idea>
    ): TodayState {
        val today = timeProvider.today()
        val logsByHabit = logs.groupBy { it.habitId }
        val completedDaysByHabit = logsByHabit.mapValues { (_, habitLogs) ->
            habitLogs.mapTo(mutableSetOf()) { it.date }
        }
        val due = progress
            .filter { Weekdays.isGoalDay(today, it.habit.goalDays) }
            .sortedWith(compareBy({ it.completedToday }, { it.habit.sortOrder }, { it.habit.id }))
        val overview = StatsCalculator.todayOverview(progress.map { it.habit }, logs, today)
        val activeIds = progress.mapTo(mutableSetOf()) { it.habit.id }
        val relevantIdeas = ideas.filter { it.habitId in activeIds }
        val idea = if (ideaEnabled) {
            val candidates = due.filterNot { it.completedToday } + due + progress
            val candidate = candidates.firstOrNull { item ->
                relevantIdeas.any { it.habitId == item.habit.id }
            }
            candidate?.let { item ->
                val habitIdeas = relevantIdeas.filter { it.habitId == item.habit.id }
                habitIdeas.firstOrNull()?.let { latest ->
                    TodayIdea(
                        habitId = item.habit.id,
                        habitName = item.habit.name,
                        title = latest.title,
                        text = latest.text,
                        model = latest.model,
                        earlierText = habitIdeas.drop(1).firstOrNull()?.text
                    )
                }
            }
        } else {
            null
        }
        return TodayState(
            isLoading = false,
            today = today,
            greeting = greetingNow(),
            displayName = name,
            dueHabits = due,
            allActiveHabits = progress,
            overview = overview,
            completedDaysByHabit = completedDaysByHabit,
            idea = idea,
            ideaEngineEnabled = ideaEnabled
        )
    }

    private fun greetingNow(): TodayGreeting {
        val hour = Instant.ofEpochMilli(timeProvider.nowMillis())
            .atZone(ZoneId.systemDefault())
            .hour
        return when {
            hour < 12 -> TodayGreeting.MORNING
            hour < 18 -> TodayGreeting.AFTERNOON
            else -> TodayGreeting.EVENING
        }
    }
}
