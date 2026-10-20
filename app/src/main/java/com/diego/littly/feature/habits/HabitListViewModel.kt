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
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitListState(
    val isLoading: Boolean = true,
    val habits: List<HabitWithProgress> = emptyList(),
    val archived: List<Habit> = emptyList()
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    observeTodayHabits: ObserveTodayHabits,
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider,
    private val reminderPlanner: ReminderPlanner,
    private val ideaQueueFiller: IdeaQueueFiller
) : ViewModel() {

    val state: StateFlow<HabitListState> =
        combine(
            observeTodayHabits(),
            habitRepository.observeArchivedHabits()
        ) { active, archived ->
            HabitListState(isLoading = false, habits = active, archived = archived)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HabitListState()
        )

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
