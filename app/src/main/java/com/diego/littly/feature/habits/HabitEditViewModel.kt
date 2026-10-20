package com.diego.littly.feature.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.Reminder
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.time.TimeProvider
import com.diego.littly.core.domain.usecase.IdeaQueueFiller
import com.diego.littly.core.notifications.ReminderPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitEditState(
    val isLoading: Boolean = true,
    val isEdit: Boolean = false,
    val name: String = "",
    val goalDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminderEnabled: Boolean = false,
    val reminderMinuteOfDay: Int? = null,
    val colorIndex: Int = 0,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = name.isNotBlank() && goalDays.isNotEmpty()
}

@HiltViewModel
class HabitEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    private val timeProvider: TimeProvider,
    private val reminderPlanner: ReminderPlanner,
    private val ideaQueueFiller: IdeaQueueFiller
) : ViewModel() {

    private val habitId: Long? = savedStateHandle.get<Long>("habitId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(
        HabitEditState(
            isLoading = habitId != null,
            isEdit = habitId != null
        )
    )
    val state: StateFlow<HabitEditState> = _state.asStateFlow()

    init {
        habitId?.let { id ->
            viewModelScope.launch {
                habitRepository.getHabit(id)?.let { habit ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = habit.name,
                            goalDays = habit.goalDays,
                            reminderEnabled = habit.reminder.enabled,
                            reminderMinuteOfDay = habit.reminder.minuteOfDay,
                            colorIndex = habit.colorIndex
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onGoalDaysChange(days: Set<DayOfWeek>) {
        _state.update { it.copy(goalDays = days) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _state.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderTimeChange(minuteOfDay: Int) {
        _state.update { it.copy(reminderMinuteOfDay = minuteOfDay) }
    }

    fun onColorChange(colorIndex: Int) {
        _state.update { it.copy(colorIndex = colorIndex) }
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        viewModelScope.launch {
            val reminder = Reminder(
                enabled = current.reminderEnabled,
                minuteOfDay = if (current.reminderEnabled) current.reminderMinuteOfDay else null
            )
            val savedHabitId = if (current.isEdit) {
                val existing = habitRepository.getHabit(habitId!!) ?: return@launch
                habitRepository.updateHabit(
                    existing.copy(
                        name = current.name.trim(),
                        goalDays = current.goalDays,
                        reminder = reminder,
                        colorIndex = current.colorIndex
                    )
                )
                existing.id
            } else {
                habitRepository.addHabit(
                    name = current.name.trim(),
                    goalDays = current.goalDays,
                    reminder = reminder,
                    colorIndex = current.colorIndex
                )
            }
            reminderPlanner.rescheduleHabit(savedHabitId)
            ideaQueueFiller.fillQueue(savedHabitId)
            _state.update { it.copy(saved = true) }
        }
    }

    fun consumeSaved() {
        _state.update { it.copy(saved = false) }
    }
}
