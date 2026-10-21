package com.diego.littly.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.diego.littly.R
import com.diego.littly.core.domain.model.Weekdays
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun HabitHistoryGrid(
    completedDays: Set<LocalDate>,
    goalDays: Set<DayOfWeek>,
    createdAt: LocalDate,
    today: LocalDate,
    accentColor: Color,
    weeks: Int = 13,
    modifier: Modifier = Modifier
) {
    val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val visibleDates = (0 until weeks).flatMap { column ->
        (0..6).map { row ->
            currentMonday.minusWeeks((weeks - 1 - column).toLong()).plusDays(row.toLong())
        }
    }.filter { !it.isBefore(createdAt) && !it.isAfter(today) }
    val doneCount = visibleDates.count { it in completedDays }
    val goalCount = visibleDates.count { Weekdays.isGoalDay(it, goalDays) }
    val description = stringResource(R.string.history_accessibility, doneCount, goalCount)
    val goalColor = MaterialTheme.colorScheme.surfaceVariant
    val restColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val emptyColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    Column(
        modifier = modifier.semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(7) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(weeks) { column ->
                    val date = currentMonday
                        .minusWeeks((weeks - 1 - column).toLong())
                        .plusDays(row.toLong())
                    val visible = !date.isBefore(createdAt) && !date.isAfter(today)
                    val isGoal = Weekdays.isGoalDay(date, goalDays)
                    val color = when {
                        !visible -> Color.Transparent
                        date in completedDays -> accentColor
                        isGoal -> goalColor
                        else -> restColor
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, RoundedCornerShape(5.dp))
                            .then(
                                if (date == today) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                } else if (visible && !isGoal) {
                                    Modifier.border(1.dp, emptyColor, RoundedCornerShape(5.dp))
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }
    }
}
