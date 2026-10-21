package com.diego.littly.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.repository.HabitRepository
import com.diego.littly.core.domain.stats.BestStreak
import com.diego.littly.core.domain.stats.MonthRate
import com.diego.littly.core.domain.stats.MonthSummary
import com.diego.littly.core.domain.stats.MonthTrendPoint
import com.diego.littly.core.domain.stats.StatsCalculator
import com.diego.littly.core.domain.stats.TotalCheckIns
import com.diego.littly.core.domain.time.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class StatsState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthSummary: MonthSummary? = null,
    val previousMonthRate: MonthRate? = null,
    val trend: List<MonthTrendPoint> = emptyList(),
    val bestStreak: BestStreak? = null,
    val totalCheckIns: TotalCheckIns = TotalCheckIns(0, null),
    val habits: List<Habit> = emptyList(),
    val canGoNext: Boolean = false
) {
    val hasHabits: Boolean
        get() = habits.isNotEmpty()

    val hasCheckIns: Boolean
        get() = totalCheckIns.count > 0
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    habitRepository: HabitRepository,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.from(timeProvider.today()))
    private val reload = MutableStateFlow(0)

    val state: StateFlow<StatsState> =
        reload.flatMapLatest {
            combine(
                selectedMonth,
                habitRepository.observeActiveHabits(),
                habitRepository.observeArchivedHabits(),
                habitRepository.observeAllLogs()
            ) { month, active, archived, logs ->
                val today = timeProvider.today()
                val allHabits = active + archived
                val logsByHabit = logs.groupBy { it.habitId }
                val summary = StatsCalculator.monthSummary(allHabits, logsByHabit, month, today)
                val previous = StatsCalculator.monthRate(
                    habits = allHabits,
                    logsByHabit = logsByHabit,
                    month = month.minusMonths(1),
                    today = today
                )
                StatsState(
                    isLoading = false,
                    today = today,
                    selectedMonth = month,
                    monthSummary = summary,
                    previousMonthRate = previous,
                    trend = StatsCalculator.trend(
                        habits = allHabits,
                        logsByHabit = logsByHabit,
                        endMonth = month,
                        today = today,
                        monthCount = 6
                    ),
                    bestStreak = StatsCalculator.bestStreak(allHabits, logsByHabit, today),
                    totalCheckIns = StatsCalculator.totalCheckIns(logs),
                    habits = allHabits,
                    canGoNext = month < YearMonth.from(today)
                )
            }.catch {
                emit(StatsState(isLoading = false, hasError = true))
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsState()
        )

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        val todayMonth = YearMonth.from(timeProvider.today())
        if (selectedMonth.value < todayMonth) {
            selectedMonth.value = selectedMonth.value.plusMonths(1)
        }
    }

    fun retry() {
        reload.value = reload.value + 1
    }
}
