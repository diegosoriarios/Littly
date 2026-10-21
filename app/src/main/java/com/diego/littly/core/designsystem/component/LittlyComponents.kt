package com.diego.littly.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diego.littly.R
import com.diego.littly.core.designsystem.theme.LittlyRadii
import com.diego.littly.core.designsystem.theme.LittlySpacing
import com.diego.littly.core.designsystem.theme.littly
import com.diego.littly.core.domain.model.Habit
import com.diego.littly.core.domain.model.Weekdays
import java.time.LocalDate

@Composable
fun LittlyCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shape: RoundedCornerShape = RoundedCornerShape(LittlyRadii.card),
    contentPadding: PaddingValues = PaddingValues(LittlySpacing.lg),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val containerModifier = modifier
        .clip(shape)
        .background(color)
        .border(1.dp, borderColor, shape)
    if (onClick != null) {
        Box(
            modifier = containerModifier.clickable(role = Role.Button, onClick = onClick)
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    } else {
        Box(containerModifier) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

@Composable
fun LittlyPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
        label = "pillContainer"
    )
    val content = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        border = BorderStroke(1.dp, if (selected) container else MaterialTheme.colorScheme.outline),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

@Composable
fun LittlyIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun HabitIconTile(
    habit: Habit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    solid: Boolean = false
) {
    val palette = habitAccent(habit.colorIndex)
    val container = if (solid) palette.accent else palette.soft
    val tint = if (solid) readableOn(palette.accent) else palette.accent
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(if (size >= 48.dp) LittlyRadii.icon else 14.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = habitIcon(habit.iconKey).imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}

@Composable
fun CompletionControl(
    completed: Boolean,
    accent: Color,
    onToggle: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val container by animateColorAsState(
        targetValue = if (completed) MaterialTheme.littly.success else MaterialTheme.colorScheme.surface,
        label = "completionContainer"
    )
    val border by animateColorAsState(
        targetValue = if (completed) MaterialTheme.littly.success else MaterialTheme.colorScheme.outline,
        label = "completionBorder"
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(container)
            .border(1.5.dp, border, CircleShape)
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.littly.onSuccess,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.65f))
            )
        }
    }
}

enum class ActivityDotState { DONE, GOAL, REST }

fun activityDots(
    habit: Habit,
    completedDays: Set<LocalDate>,
    today: LocalDate,
    count: Int = 10
): List<ActivityDotState> =
    (count - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        when {
            date.isBefore(habit.createdAt) -> ActivityDotState.REST
            date in completedDays -> ActivityDotState.DONE
            Weekdays.isGoalDay(date, habit.goalDays) -> ActivityDotState.GOAL
            else -> ActivityDotState.REST
        }
    }

@Composable
fun ActivityDots(
    states: List<ActivityDotState>,
    accent: Color,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    val done = states.count { it == ActivityDotState.DONE }
    val goals = states.count { it != ActivityDotState.REST }
    val semanticsModifier = if (description != null) {
        modifier.semantics { contentDescription = description }
    } else {
        modifier.semantics { contentDescription = "$done of $goals recent goal days done" }
    }
    Row(
        modifier = semanticsModifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        states.forEach { state ->
            val color = when (state) {
                ActivityDotState.DONE -> accent
                ActivityDotState.GOAL -> accent.copy(alpha = 0.22f)
                ActivityDotState.REST -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
                    .then(
                        if (state == ActivityDotState.GOAL) {
                            Modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(3.dp))
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
fun LittlyAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.littly.success),
        contentAlignment = Alignment.Center
    ) {
        if (initial != null) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.littly.onSuccess
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.LocalFlorist,
                contentDescription = null,
                tint = MaterialTheme.littly.onSuccess,
                modifier = Modifier.size(size * 0.44f)
            )
        }
    }
}

@Composable
fun ProgressRing(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 82.dp,
    trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.littly.onNavy,
    centerText: String = "$completed/$total",
    caption: String = stringResource(R.string.today_ring_today)
) {
    val progress = if (total <= 0) 0f else (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = size.toPx() * 0.11f
            val inset = stroke / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - stroke, this.size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleMedium,
                color = labelColor
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
fun LittlyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 15.dp),
        modifier = modifier.defaultMinSize(minHeight = 52.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun LittlySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier.defaultMinSize(minHeight = 52.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LittlySpacing.xl, vertical = LittlySpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(LittlyRadii.tile))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(LittlySpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(LittlySpacing.xs))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(LittlySpacing.lg))
            action()
        }
    }
}

@Composable
fun LittlyTopBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LittlySpacing.md, vertical = LittlySpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.widthIn(min = 48.dp)) {
            if (onBack != null) {
                LittlyIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    onClick = onBack
                )
            }
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(LittlySpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
        }
    }
}
