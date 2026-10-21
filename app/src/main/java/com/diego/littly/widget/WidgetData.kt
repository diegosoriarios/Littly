package com.diego.littly.widget

import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.HabitLog
import com.diego.littly.core.domain.model.Idea
import com.diego.littly.core.domain.model.Weekdays
import com.diego.littly.core.domain.streak.StreakCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetHabitItem(
    val habitId: Long,
    val name: String,
    val colorIndex: Int,
    val completed: Boolean,
    val currentStreak: Int
)

data class OverviewWidgetData(
    val dateLabel: String,
    val completed: Int,
    val due: Int,
    val bestStreak: Int,
    val bestStreakName: String?,
    val items: List<WidgetHabitItem>,
    val hasActiveHabits: Boolean,
    val fallbackToAllHabits: Boolean
)

data class StreakWidgetData(
    val habitId: Long,
    val habitName: String,
    val streak: Int
)

data class IdeaWidgetData(
    val enabled: Boolean,
    val available: Boolean,
    val habitId: Long?,
    val habitName: String?,
    val headline: String?,
    val body: String?,
    val model: String?
)

object WidgetDataSelector {

    const val MAX_ITEMS = 5

    /**
     * The overview uses 48dp rows, a 48dp header, and 24dp of outer padding.
     * Returning fewer items keeps the widget readable at the launcher size.
     */
    fun itemLimit(heightDp: Float): Int {
        if (!heightDp.isFinite() || heightDp <= 0f) return MAX_ITEMS
        return (((heightDp - 72f) / 48f).toInt()).coerceIn(1, MAX_ITEMS)
    }

    fun overview(
        habits: List<Habit>,
        logs: List<HabitLog>,
        today: LocalDate
    ): OverviewWidgetData {
        val active = habits.filter { it.archivedAt == null }
        val logsByHabit = logs.groupBy { it.habitId }
        val withProgress = active.map { habit ->
            val completedDays = logsByHabit[habit.id].orEmpty().mapTo(mutableSetOf()) { it.date }
            WidgetHabitItem(
                habitId = habit.id,
                name = habit.name,
                colorIndex = habit.colorIndex,
                completed = today in completedDays,
                currentStreak = StreakCalculator.currentStreak(
                    completedDays = completedDays,
                    goalDays = habit.goalDays,
                    today = today,
                    createdAt = habit.createdAt
                )
            )
        }
        val due = withProgress.filter { item ->
            val habit = active.first { it.id == item.habitId }
            Weekdays.isGoalDay(today, habit.goalDays)
        }
        val hasDue = due.isNotEmpty()
        val visible = if (hasDue) due else withProgress
        val best = withProgress.maxByOrNull { it.currentStreak }
        return OverviewWidgetData(
            dateLabel = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())),
            completed = due.count { it.completed },
            due = due.size,
            bestStreak = best?.currentStreak ?: 0,
            bestStreakName = best?.name,
            items = visible.take(MAX_ITEMS),
            hasActiveHabits = active.isNotEmpty(),
            fallbackToAllHabits = !hasDue && withProgress.isNotEmpty()
        )
    }

    fun streak(
        habits: List<Habit>,
        logs: List<HabitLog>,
        today: LocalDate
    ): StreakWidgetData? {
        val logsByHabit = logs.groupBy { it.habitId }
        return habits.filter { it.archivedAt == null }
            .map { habit ->
                val completedDays = logsByHabit[habit.id].orEmpty().mapTo(mutableSetOf()) { it.date }
                habit to StreakCalculator.currentStreak(
                    completedDays = completedDays,
                    goalDays = habit.goalDays,
                    today = today,
                    createdAt = habit.createdAt
                )
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
            ?.let { (habit, streak) ->
                StreakWidgetData(
                    habitId = habit.id,
                    habitName = habit.name,
                    streak = streak
                )
            }
    }

    fun idea(
        habits: List<Habit>,
        ideas: List<Idea>,
        ideaEngineEnabled: Boolean
    ): IdeaWidgetData {
        if (!ideaEngineEnabled) {
            return IdeaWidgetData(
                enabled = false,
                available = false,
                habitId = null,
                habitName = null,
                headline = null,
                body = null,
                model = null
            )
        }
        val active = habits.filter { it.archivedAt == null }.associateBy { it.id }
        val latest = ideas.firstOrNull { it.habitId in active.keys }
        val habit = latest?.let { active[it.habitId] }
        if (latest == null || habit == null) {
            return IdeaWidgetData(
                enabled = true,
                available = false,
                habitId = null,
                habitName = null,
                headline = null,
                body = null,
                model = null
            )
        }
        val title = latest.title?.takeIf { it.isNotBlank() }
        return IdeaWidgetData(
            enabled = true,
            available = true,
            habitId = habit.id,
            habitName = habit.name,
            headline = title ?: latest.text,
            body = if (title != null) latest.text else null,
            model = latest.model
        )
    }
}
