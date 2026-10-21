package com.diego.littly.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.diego.littly.core.designsystem.theme.HabitAccent
import com.diego.littly.core.designsystem.theme.LittlyHabitPalette

fun habitAccent(colorIndex: Int): HabitAccent =
    LittlyHabitPalette[Math.floorMod(colorIndex, LittlyHabitPalette.size)]

fun habitColor(colorIndex: Int): Color = habitAccent(colorIndex).accent

fun readableOn(background: Color): Color {
    val light = Color(0xFFFFFCF8)
    val dark = Color(0xFF1E2B39)
    return if (contrastRatio(light, background) >= contrastRatio(dark, background)) light else dark
}

private fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}
