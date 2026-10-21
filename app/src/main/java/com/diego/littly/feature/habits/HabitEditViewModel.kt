package com.diego.littly.feature.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitIcon
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
    val notFound: Boolean = false,
    val name: String = "",
    val description: String = "",
    val iconKey: String = HabitIcon.default.key,
    val goalDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminderEnabled: Boolean = false,
    val reminderMinuteOfDay: Int? = null,
    val reminderDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val colorIndex: Int = 0,
    val saved: Boolean = false,
    val showValidation: Boolean = false
) {
    val nameError: Boolean
        get() = showValidation && name.isBlank()

    val goalDaysError: Boolean
        get() = showValidation && goalDays.isEmpty()

    val reminderDaysError: Boolean
        get() = showValidation && reminderEnabled && reminderDays.isEmpty()

    val canSave: Boolean
        get() = name.isNotBlank() &&
            goalDays.isNotEmpty() &&
            (!reminderEnabled || reminderDays.isNotEmpty())
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
                val habit = habitRepository.getHabit(id)
                if (habit == null) {
                    _state.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = habit.name,
                            description = habit.description.orEmpty(),
                            iconKey = habit.iconKey,
                            goalDays = habit.goalDays,
                            reminderEnabled = habit.reminder.enabled,
                            reminderMinuteOfDay = habit.reminder.minuteOfDay,
                            reminderDays = habit.reminder.days ?: habit.goalDays,
                            colorIndex = habit.colorIndex
                        )
                    }
                }
            }
        } ?: run {
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun onIconChange(iconKey: String) {
        _state.update { it.copy(iconKey = iconKey) }
    }

    fun onGoalDaysChange(days: Set<DayOfWeek>) {
        _state.update { current ->
            val syncReminder = current.reminderDays == current.goalDays
            current.copy(
                goalDays = days,
                reminderDays = if (syncReminder) days else current.reminderDays
            )
        }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _state.update { current ->
            current.copy(
                reminderEnabled = enabled,
                reminderMinuteOfDay = if (enabled) {
                    current.reminderMinuteOfDay ?: DEFAULT_REMINDER_MINUTE
                } else {
                    current.reminderMinuteOfDay
                },
                reminderDays = if (enabled && current.reminderDays.isEmpty()) {
                    current.goalDays
                } else {
                    current.reminderDays
                }
            )
        }
    }

    fun onReminderTimeChange(minuteOfDay: Int) {
        _state.update { it.copy(reminderMinuteOfDay = minuteOfDay) }
    }

    fun onReminderDaysChange(days: Set<DayOfWeek>) {
        _state.update { it.copy(reminderDays = days) }
    }

    fun onColorChange(colorIndex: Int) {
        _state.update { it.copy(colorIndex = colorIndex) }
    }

    fun save() {
        val current = _state.value
        if (!current.canSave) {
            _state.update { it.copy(showValidation = true) }
            return
        }
        viewModelScope.launch {
            val reminder = Reminder(
                enabled = current.reminderEnabled,
                minuteOfDay = if (current.reminderEnabled) {
                    current.reminderMinuteOfDay ?: DEFAULT_REMINDER_MINUTE
                } else {
                    null
                },
                days = if (current.reminderEnabled) current.reminderDays else null
            )
            val savedHabitId = if (current.isEdit) {
                val existing = habitRepository.getHabit(habitId!!) ?: return@launch
                habitRepository.updateHabit(
                    existing.copy(
                        name = current.name.trim(),
                        description = current.description.trim().takeIf { it.isNotEmpty() },
                        iconKey = current.iconKey,
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
                    colorIndex = current.colorIndex,
                    description = current.description.trim().takeIf { it.isNotEmpty() },
                    iconKey = current.iconKey
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

    private companion object {
        const val DEFAULT_REMINDER_MINUTE = 9 * 60
    }
}
