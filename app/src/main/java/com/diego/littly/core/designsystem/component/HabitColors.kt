package com.diego.littly.core.designsystem.component

import androidx.compose.ui.graphics.Color

val LittlyHabitPalette = listOf(
    Color(0xFFD96F4E),
    Color(0xFF8FA968),
    Color(0xFFD9A441),
    Color(0xFF6FA3A0),
    Color(0xFFA98BA3),
    Color(0xFFC97B8E)
)

fun habitColor(colorIndex: Int): Color =
    LittlyHabitPalette[Math.floorMod(colorIndex, LittlyHabitPalette.size)]

fun readableOn(color: Color): Color =
    if (color.luminanceCompat() > 0.6f) Color(0xFF3A2A20) else Color.White

private fun Color.luminanceCompat(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
