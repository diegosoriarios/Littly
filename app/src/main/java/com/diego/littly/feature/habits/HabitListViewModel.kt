package com.diego.littly.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.CompletionSource
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitWithProgress
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.IdeaQueueFiller
import com.diego.littly.core.domain.usecase.ObserveTodayHabits
import com.diego.littly.core.notifications.ReminderPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HabitFilter { ALL, MORNING, AFTERNOON, EVENING, PAUSED }

data class HabitListState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val filter: HabitFilter = HabitFilter.ALL,
    val today: LocalDate = LocalDate.now(),
    val habits: List<HabitWithProgress> = emptyList(),
    val archived: List<Habit> = emptyList(),
    val completedDaysByHabit: Map<Long, Set<LocalDate>> = emptyMap(),
    val bestHabitId: Long? = null
) {
    val activeCount: Int
        get() = habits.size

    val visibleActive: List<HabitWithProgress>
        get() = when (filter) {
            HabitFilter.ALL -> habits
            HabitFilter.MORNING -> habits.filter { it.habit.inReminderRange(0, 12) }
            HabitFilter.AFTERNOON -> habits.filter { it.habit.inReminderRange(12, 17) }
            HabitFilter.EVENING -> habits.filter { it.habit.inReminderRange(17, 24) }
            HabitFilter.PAUSED -> emptyList()
        }

    val visibleArchived: List<Habit>
        get() = if (filter == HabitFilter.PAUSED || (filter == HabitFilter.ALL && habits.isEmpty())) {
            archived
        } else {
            emptyList()
        }

    val isEmpty: Boolean
        get() = habits.isEmpty() && archived.isEmpty()

    val isFilterEmpty: Boolean
        get() = !isEmpty && visibleActive.isEmpty() && visibleArchived.isEmpty()
}

private fun Habit.inReminderRange(startHour: Int, endHour: Int): Boolean {
    val minute = reminder.minuteOfDay ?: return false
    if (!reminder.enabled) return false
    val hour = minute / 60
    return hour >= startHour && hour < endHour
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HabitListViewModel @Inject constructor(
    observeTodayHabits: ObserveTodayHabits,
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider,
    private val reminderPlanner: ReminderPlanner,
    private val ideaQueueFiller: IdeaQueueFiller
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(HabitFilter.ALL)
    private val reload = MutableStateFlow(0)

    val state: StateFlow<HabitListState> =
        reload.flatMapLatest {
            combine(
                selectedFilter,
                observeTodayHabits(),
                habitRepository.observeArchivedHabits(),
                habitRepository.observeAllLogs()
            ) { filter, active, archived, logs ->
                val logsByHabit = logs.groupBy { it.habitId }
                val best = active.maxByOrNull { it.currentStreak }
                HabitListState(
                    isLoading = false,
                    filter = filter,
                    today = timeProvider.today(),
                    habits = active,
                    archived = archived,
                    completedDaysByHabit = logsByHabit.mapValues { (_, habitLogs) ->
                        habitLogs.mapTo(mutableSetOf()) { it.date }
                    },
                    bestHabitId = best?.takeIf { it.currentStreak > 0 }?.habit?.id
                )
            }.catch {
                emit(HabitListState(isLoading = false, hasError = true))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitListState()
        )

    fun setFilter(filter: HabitFilter) {
        selectedFilter.value = filter
    }

    fun retry() {
        reload.value = reload.value + 1
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch {
            habitRepository.toggleCompletion(habitId, timeProvider.today(), CompletionSource.APP)
        }
    }

    fun setArchived(habitId: Long, archived: Boolean) {
        viewModelScope.launch {
            habitRepository.setArchived(habitId, archived)
            if (archived) {
                reminderPlanner.cancelHabit(habitId)
            } else {
                reminderPlanner.rescheduleHabit(habitId)
                ideaQueueFiller.fillQueue(habitId)
            }
        }
    }
}
