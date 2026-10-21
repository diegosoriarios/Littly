package com.diego.littly.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFF5F0E7)
val WarmSurface = Color(0xFFFFFBF5)
val WarmSurfaceLow = Color(0xFFFCF7EF)
val WarmSurfaceHigh = Color(0xFFF1E9DD)
val WarmBorder = Color(0xFFEADCCB)
val Navy = Color(0xFF1E2B39)
val NavySoft = Color(0xFF2A3745)
val SecondaryText = Color(0xFF6F6152)
val MutedText = Color(0xFFB5A591)
val Orange = Color(0xFFCC6934)
val OrangeDeep = Color(0xFFB25527)
val SoftOrange = Color(0xFFF5D9C7)
val OnDark = Color(0xFFFFFCF8)
val IdeaBackground = Color(0xFFFCF0D3)
val IdeaBorder = Color(0xFFEED79E)
val IdeaAccent = Color(0xFFD8AA35)
val Success = Color(0xFF557A5F)
val SuccessSoft = Color(0xFFDEE7D7)

val DarkBackground = Color(0xFF141A20)
val DarkSurface = Color(0xFF1C242D)
val DarkSurfaceLow = Color(0xFF182028)
val DarkSurfaceHigh = Color(0xFF2A333D)
val DarkBorder = Color(0xFF3D454E)
val DarkOnSurface = Color(0xFFFFFCF8)
val DarkSecondaryText = Color(0xFFC9BCAD)
val DarkMutedText = Color(0xFF9D9285)
val DarkOrange = Color(0xFFE88650)
val DarkSoftOrange = Color(0xFF4E2B1B)
val DarkIdeaBackground = Color(0xFF352B17)
val DarkIdeaBorder = Color(0xFF5C4A22)
val DarkIdeaAccent = Color(0xFFE8C15D)
val DarkSuccess = Color(0xFF9CBCA2)
val DarkSuccessSoft = Color(0xFF2D4634)
val DarkNavyCard = Color(0xFF0E151C)
val DarkNavyCardSoft = Color(0xFF1A232C)

@Immutable
data class HabitAccent(
    val accent: Color,
    val soft: Color,
    val onSoft: Color
)

val HabitPink = HabitAccent(
    accent = Color(0xFFE686A1),
    soft = Color(0xFFFBE5E9),
    onSoft = Navy
)
val HabitTeal = HabitAccent(
    accent = Color(0xFF4DA3A6),
    soft = Color(0xFFDDECE8),
    onSoft = Navy
)
val HabitSage = HabitAccent(
    accent = Color(0xFF6C9274),
    soft = Color(0xFFDEE7D7),
    onSoft = Navy
)
val HabitPeach = HabitAccent(
    accent = Color(0xFFE99150),
    soft = Color(0xFFF8E1CF),
    onSoft = Navy
)
val HabitLavender = HabitAccent(
    accent = Color(0xFF9B85CA),
    soft = Color(0xFFEBE4F0),
    onSoft = Navy
)
val HabitMustard = HabitAccent(
    accent = Color(0xFFD6AA3B),
    soft = Color(0xFFF2DFA8),
    onSoft = Navy
)

val LittlyHabitPalette = listOf(
    HabitPink,
    HabitTeal,
    HabitSage,
    HabitPeach,
    HabitLavender,
    HabitMustard
)

@Immutable
data class LittlyExtraColors(
    val navyCard: Color,
    val navyCardSoft: Color,
    val onNavy: Color,
    val onNavyMuted: Color,
    val success: Color,
    val successSoft: Color,
    val onSuccess: Color,
    val ideaBackground: Color,
    val ideaBorder: Color,
    val ideaAccent: Color,
    val ideaLabel: Color,
    val onIdea: Color,
    val mutedText: Color,
    val habitPalette: List<HabitAccent>,
    val isDark: Boolean
)

val LightExtraColors = LittlyExtraColors(
    navyCard = Navy,
    navyCardSoft = NavySoft,
    onNavy = OnDark,
    onNavyMuted = Color(0xFFB9C1C9),
    success = Success,
    successSoft = SuccessSoft,
    onSuccess = Color(0xFFFFFFFF),
    ideaBackground = IdeaBackground,
    ideaBorder = IdeaBorder,
    ideaAccent = IdeaAccent,
    ideaLabel = Color(0xFF8A6514),
    onIdea = Navy,
    mutedText = MutedText,
    habitPalette = LittlyHabitPalette,
    isDark = false
)

val DarkExtraColors = LittlyExtraColors(
    navyCard = DarkNavyCard,
    navyCardSoft = DarkNavyCardSoft,
    onNavy = OnDark,
    onNavyMuted = Color(0xFFAEB8C2),
    success = DarkSuccess,
    successSoft = DarkSuccessSoft,
    onSuccess = Color(0xFF122016),
    ideaBackground = DarkIdeaBackground,
    ideaBorder = DarkIdeaBorder,
    ideaAccent = DarkIdeaAccent,
    ideaLabel = DarkIdeaAccent,
    onIdea = Color(0xFFFFF4DA),
    mutedText = DarkMutedText,
    habitPalette = LittlyHabitPalette,
    isDark = true
)
