package com.diego.littly.widget

import androidx.compose.ui.graphics.Color
import com.diego.littly.core.domain.model.ThemeMode

data class WidgetPalette(
    val background: Color,
    val card: Color,
    val cardMuted: Color,
    val text: Color,
    val mutedText: Color,
    val accent: Color,
    val onAccent: Color,
    val success: Color,
    val onSuccess: Color,
    val border: Color,
    val ideaBackground: Color,
    val ideaBorder: Color,
    val ideaText: Color,
    val ideaAccent: Color,
    val ideaLabel: Color,
    val onNavyMuted: Color,
    val navy: Color,
    val onNavy: Color,
    val isDark: Boolean
)

object WidgetTheme {

    val light = WidgetPalette(
        background = Color(0xFFF5F0E7),
        card = Color(0xFFFFFBF5),
        cardMuted = Color(0xFFF1E9DD),
        text = Color(0xFF1E2B39),
        mutedText = Color(0xFF6F6152),
        accent = Color(0xFFB25527),
        onAccent = Color(0xFFFFFCF8),
        success = Color(0xFF557A5F),
        onSuccess = Color(0xFFFFFFFF),
        border = Color(0xFFEADCCB),
        ideaBackground = Color(0xFFFCF0D3),
        ideaBorder = Color(0xFFEED79E),
        ideaText = Color(0xFF1E2B39),
        ideaAccent = Color(0xFFD8AA35),
        ideaLabel = Color(0xFF8A6514),
        onNavyMuted = Color(0xFFB9C1C9),
        navy = Color(0xFF1E2B39),
        onNavy = Color(0xFFFFFCF8),
        isDark = false
    )

    val dark = WidgetPalette(
        background = Color(0xFF141A20),
        card = Color(0xFF1C242D),
        cardMuted = Color(0xFF2A333D),
        text = Color(0xFFFFFCF8),
        mutedText = Color(0xFFC9BCAD),
        accent = Color(0xFFE88650),
        onAccent = Color(0xFF2E1408),
        success = Color(0xFF9CBCA2),
        onSuccess = Color(0xFF122016),
        border = Color(0xFF3D454E),
        ideaBackground = Color(0xFF352B17),
        ideaBorder = Color(0xFF5C4A22),
        ideaText = Color(0xFFFFF4DA),
        ideaAccent = Color(0xFFE8C15D),
        ideaLabel = Color(0xFFE8C15D),
        onNavyMuted = Color(0xFFAEB8C2),
        navy = Color(0xFF0E151C),
        onNavy = Color(0xFFFFFCF8),
        isDark = true
    )

    fun resolve(themeMode: ThemeMode, systemDark: Boolean): WidgetPalette = when (themeMode) {
        ThemeMode.SYSTEM -> if (systemDark) dark else light
        ThemeMode.LIGHT -> light
        ThemeMode.DARK -> dark
    }

    fun accentFor(colorIndex: Int): Color {
        val accents = listOf(
            Color(0xFFE686A1),
            Color(0xFF4DA3A6),
            Color(0xFF6C9274),
            Color(0xFFE99150),
            Color(0xFF9B85CA),
            Color(0xFFD6AA3B)
        )
        return accents[Math.floorMod(colorIndex, accents.size)]
    }
}
