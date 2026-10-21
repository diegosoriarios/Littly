package com.diego.littly.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.diego.littly.core.domain.model.ThemeMode

val LocalLittlyExtraColors = staticCompositionLocalOf { LightExtraColors }

val MaterialTheme.littly: LittlyExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalLittlyExtraColors.current

private val LightColorScheme = lightColorScheme(
    primary = OrangeDeep,
    onPrimary = OnDark,
    primaryContainer = SoftOrange,
    onPrimaryContainer = ColorTokens.BurntBrown,
    secondary = Success,
    onSecondary = OnDark,
    secondaryContainer = SuccessSoft,
    onSecondaryContainer = ColorTokens.DeepSage,
    tertiary = IdeaAccent,
    onTertiary = ColorTokens.DeepMustard,
    tertiaryContainer = IdeaBackground,
    onTertiaryContainer = ColorTokens.DeepMustard,
    error = ColorTokens.Error,
    onError = ColorTokens.OnError,
    errorContainer = ColorTokens.ErrorSoft,
    onErrorContainer = ColorTokens.DeepError,
    background = Cream,
    onBackground = Navy,
    surface = WarmSurface,
    onSurface = Navy,
    surfaceVariant = WarmSurfaceHigh,
    onSurfaceVariant = SecondaryText,
    outline = WarmBorder,
    outlineVariant = WarmBorder,
    surfaceContainerLowest = WarmSurface,
    surfaceContainerLow = WarmSurfaceLow,
    surfaceContainer = WarmSurface,
    surfaceContainerHigh = WarmSurfaceHigh,
    surfaceContainerHighest = WarmSurfaceHigh,
    surfaceTint = Orange,
    inverseSurface = Navy,
    inverseOnSurface = OnDark,
    inversePrimary = SoftOrange
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrange,
    onPrimary = ColorTokens.DarkOnPrimary,
    primaryContainer = DarkSoftOrange,
    onPrimaryContainer = ColorTokens.DarkOnPrimaryContainer,
    secondary = DarkSuccess,
    onSecondary = ColorTokens.DarkOnSuccess,
    secondaryContainer = DarkSuccessSoft,
    onSecondaryContainer = ColorTokens.DarkOnSuccessContainer,
    tertiary = DarkIdeaAccent,
    onTertiary = ColorTokens.DarkOnIdea,
    tertiaryContainer = DarkIdeaBackground,
    onTertiaryContainer = ColorTokens.DarkOnIdeaContainer,
    error = ColorTokens.DarkError,
    onError = ColorTokens.DarkOnError,
    errorContainer = ColorTokens.DarkErrorSoft,
    onErrorContainer = ColorTokens.DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHigh,
    surfaceTint = DarkOrange,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkBackground,
    inversePrimary = DarkOrange
)

private object ColorTokens {
    val BurntBrown = androidx.compose.ui.graphics.Color(0xFF6B2E13)
    val DeepSage = androidx.compose.ui.graphics.Color(0xFF2C4432)
    val DeepMustard = androidx.compose.ui.graphics.Color(0xFF5D460D)
    val Error = androidx.compose.ui.graphics.Color(0xFFB3261E)
    val OnError = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val ErrorSoft = androidx.compose.ui.graphics.Color(0xFFF9DEDC)
    val DeepError = androidx.compose.ui.graphics.Color(0xFF410E0B)

    val DarkOnPrimary = androidx.compose.ui.graphics.Color(0xFF321406)
    val DarkOnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDCCB)
    val DarkOnSuccess = androidx.compose.ui.graphics.Color(0xFF0E1F13)
    val DarkOnSuccessContainer = androidx.compose.ui.graphics.Color(0xFFC9E8CF)
    val DarkOnIdea = androidx.compose.ui.graphics.Color(0xFF3A2A09)
    val DarkOnIdeaContainer = androidx.compose.ui.graphics.Color(0xFFFFF0C9)
    val DarkError = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
    val DarkOnError = androidx.compose.ui.graphics.Color(0xFF690005)
    val DarkErrorSoft = androidx.compose.ui.graphics.Color(0xFF8C1D18)
    val DarkOnErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6)
}

@Composable
fun LittlyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extras = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalLittlyExtraColors provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LittlyTypography,
            shapes = LittlyShapes,
            content = content
        )
    }
}
