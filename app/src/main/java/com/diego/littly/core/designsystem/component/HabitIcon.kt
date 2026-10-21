package com.diego.littly.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.diego.littly.core.domain.model.HabitIcon

val HabitIcon.imageVector: ImageVector
    get() = when (this) {
        HabitIcon.SPARKLES -> Icons.Rounded.AutoAwesome
        HabitIcon.BRUSH -> Icons.Rounded.Brush
        HabitIcon.BOOK -> Icons.AutoMirrored.Rounded.MenuBook
        HabitIcon.WALK -> Icons.AutoMirrored.Rounded.DirectionsWalk
        HabitIcon.MEDITATE -> Icons.Rounded.SelfImprovement
        HabitIcon.FITNESS -> Icons.Rounded.FitnessCenter
        HabitIcon.MUSIC -> Icons.Rounded.MusicNote
        HabitIcon.LEAF -> Icons.Rounded.LocalFlorist
        HabitIcon.WATER -> Icons.Rounded.WaterDrop
        HabitIcon.SLEEP -> Icons.Rounded.Bedtime
        HabitIcon.CODE -> Icons.Rounded.Code
        HabitIcon.HEART -> Icons.Rounded.Favorite
        HabitIcon.TASK -> Icons.Rounded.TaskAlt
        HabitIcon.CAMERA -> Icons.Rounded.CameraAlt
        HabitIcon.PET -> Icons.Rounded.Pets
        HabitIcon.COFFEE -> Icons.Rounded.LocalCafe
    }

fun habitIcon(key: String): HabitIcon = HabitIcon.fromKey(key)
