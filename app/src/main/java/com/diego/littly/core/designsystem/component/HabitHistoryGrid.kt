package com.diego.littly.core.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
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
    weeks: Int = 8,
    modifier: Modifier = Modifier
) {
    val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(7) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(weeks) { column ->
                    val date = currentMonday
                        .minusWeeks((weeks - 1 - column).toLong())
                        .plusDays(row.toLong())
                    val visible = !date.isBefore(createdAt) && !date.isAfter(today)
                    val color = when {
                        !visible -> Color.Transparent
                        date in completedDays -> accentColor
                        Weekdays.isGoalDay(date, goalDays) -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
