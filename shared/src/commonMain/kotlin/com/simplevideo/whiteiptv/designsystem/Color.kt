package com.simplevideo.whiteiptv.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.simplevideo.whiteiptv.domain.model.AccentColor

val Primary = Color(0xFF2badee)
val PrimaryLight = Color(0xFF0284C7) // Higher contrast for light theme (5.2:1 vs white)
val BackgroundLight = Color(0xFFf6f7f8)
val BackgroundDark = Color(0xFF101c22)
val OnPrimary = Color.White
val OnBackgroundLight = Color(0xFF101c22)
val OnBackgroundDark = Color(0xFFf6f7f8)

// Settings card colors (Stitch design tokens)
val CardDark = Color(0xFF1e2e38) // Increased contrast vs BackgroundDark (~10% luminance diff)
val CardLight = Color(0xFFFFFFFF)
val BorderDark = Color(0xFF374151)
val BorderLight = Color(0xFFe5e7eb)
val TextPrimaryDark = Color(0xFFe5e7eb)
val TextPrimaryLight = Color(0xFF111827)
val TextSecondaryDark = Color(0xFF9ca3af)
val TextSecondaryLight = Color(0xFF6b7280)
val DestructiveRed = Color(0xFFef4444)

// Card/border/text colors mapped to MaterialTheme surface roles for theme-awareness
// Use these via settingsCardColor() etc. in SettingsComponents.kt

// Slate color variants for text hierarchy and borders (matching Tailwind slate scale)
val Slate200 = Color(0xFFe2e8f0)
val Slate300 = Color(0xFFcbd5e1)
val Slate400 = Color(0xFF94a3b8)
val Slate500 = Color(0xFF64748b)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1e293b)

val PlaceholderColors = listOf(
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF26A69A), // Teal
    Color(0xFFEF5350), // Red
    Color(0xFFAB47BC), // Purple
    Color(0xFF42A5F5), // Blue
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF66BB6A), // Green
    Color(0xFFFFCA28), // Amber
)

// Keeping other colors from the original file for a complete theme
val LightPrimaryContainer = Color(0xFFEADDFF)
val LightOnPrimaryContainer = Color(0xFF21005D)
val LightSecondary = Color(0xFF625B71)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE8DEF8)
val LightOnSecondaryContainer = Color(0xFF1D192B)
val LightTertiary = Color(0xFF7D5260)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFD8E4)
val LightOnTertiaryContainer = Color(0xFF31111D)
val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF9DEDC)
val LightOnErrorContainer = Color(0xFF410E0B)

val DarkPrimaryContainer = Color(0xFF4F378B)
val DarkOnPrimaryContainer = Color(0xFFEADDFF)
val DarkSecondary = Color(0xFFCCC2DC)
val DarkOnSecondary = Color(0xFF332D41)
val DarkSecondaryContainer = Color(0xFF4A4458)
val DarkOnSecondaryContainer = Color(0xFFE8DEF8)
val DarkTertiary = Color(0xFFEFB8C8)
val DarkOnTertiary = Color(0xFF492532)
val DarkTertiaryContainer = Color(0xFF633B48)
val DarkOnTertiaryContainer = Color(0xFFFFD8E4)
val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)
val DarkErrorContainer = Color(0xFF8C1D18)
val DarkOnErrorContainer = Color(0xFFF9DEDC)

val AppLightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimary,
    background = BackgroundLight,
    surface = BackgroundLight,
    surfaceContainer = Color(0xFFF0F1F3),
    surfaceContainerHigh = Color(0xFFE8E9EB),
    onBackground = OnBackgroundLight,
    onSurface = OnBackgroundLight,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    surfaceVariant = Slate200,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
)

val AppDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = BackgroundDark,
    surface = BackgroundDark,
    onBackground = OnBackgroundDark,
    onSurface = OnBackgroundDark,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    surfaceVariant = Slate800,
    surfaceContainer = Color(0xFF1a2830),
    surfaceContainerHigh = Color(0xFF213038),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
)

// --- Teal accent (harmonized replacements for default secondary/tertiary) ---

private val TealLightSecondary = Color(0xFF4a6267)
private val TealLightOnSecondary = Color(0xFFFFFFFF)
private val TealLightSecondaryContainer = Color(0xFFcde7ed)
private val TealLightOnSecondaryContainer = Color(0xFF051f23)
private val TealLightTertiary = Color(0xFF4d6357)
private val TealLightOnTertiary = Color(0xFFFFFFFF)
private val TealLightTertiaryContainer = Color(0xFFcfe8d9)
private val TealLightOnTertiaryContainer = Color(0xFF0a2016)
private val TealLightPrimaryContainer = Color(0xFFc5e7ff)
private val TealLightOnPrimaryContainer = Color(0xFF001c38)

private val TealDarkSecondary = Color(0xFF9ecbd2)
private val TealDarkOnSecondary = Color(0xFF1b3438)
private val TealDarkSecondaryContainer = Color(0xFF324b4f)
private val TealDarkOnSecondaryContainer = Color(0xFFcde7ed)
private val TealDarkTertiary = Color(0xFF9ecbab)
private val TealDarkOnTertiary = Color(0xFF1f352a)
private val TealDarkTertiaryContainer = Color(0xFF354b40)
private val TealDarkOnTertiaryContainer = Color(0xFFcfe8d9)
private val TealDarkPrimaryContainer = Color(0xFF004a77)
private val TealDarkOnPrimaryContainer = Color(0xFFc5e7ff)

// --- Blue accent ---

private val BlueLightPrimary = Color(0xFF1a73e8)
private val BlueLightOnPrimary = Color(0xFFFFFFFF)
private val BlueLightPrimaryContainer = Color(0xFFd3e3fd)
private val BlueLightOnPrimaryContainer = Color(0xFF001c3b)
private val BlueLightSecondary = Color(0xFF535f70)
private val BlueLightOnSecondary = Color(0xFFFFFFFF)
private val BlueLightSecondaryContainer = Color(0xFFd7e3f7)
private val BlueLightOnSecondaryContainer = Color(0xFF101c2b)
private val BlueLightTertiary = Color(0xFF6b5778)
private val BlueLightOnTertiary = Color(0xFFFFFFFF)
private val BlueLightTertiaryContainer = Color(0xFFf2daff)
private val BlueLightOnTertiaryContainer = Color(0xFF251431)

private val BlueDarkPrimary = Color(0xFF8ab4f8)
private val BlueDarkOnPrimary = Color(0xFF003062)
private val BlueDarkPrimaryContainer = Color(0xFF00468a)
private val BlueDarkOnPrimaryContainer = Color(0xFFd3e3fd)
private val BlueDarkSecondary = Color(0xFFbbc7db)
private val BlueDarkOnSecondary = Color(0xFF263141)
private val BlueDarkSecondaryContainer = Color(0xFF3c4858)
private val BlueDarkOnSecondaryContainer = Color(0xFFd7e3f7)
private val BlueDarkTertiary = Color(0xFFd6bee4)
private val BlueDarkOnTertiary = Color(0xFF3b2948)
private val BlueDarkTertiaryContainer = Color(0xFF533f5f)
private val BlueDarkOnTertiaryContainer = Color(0xFFf2daff)

// --- Red accent ---

private val RedLightPrimary = Color(0xFFc62828)
private val RedLightOnPrimary = Color(0xFFFFFFFF)
private val RedLightPrimaryContainer = Color(0xFFffdad4)
private val RedLightOnPrimaryContainer = Color(0xFF410000)
private val RedLightSecondary = Color(0xFF775651)
private val RedLightOnSecondary = Color(0xFFFFFFFF)
private val RedLightSecondaryContainer = Color(0xFFffdad4)
private val RedLightOnSecondaryContainer = Color(0xFF2c1511)
private val RedLightTertiary = Color(0xFF6f5b2e)
private val RedLightOnTertiary = Color(0xFFFFFFFF)
private val RedLightTertiaryContainer = Color(0xFFfbdfa6)
private val RedLightOnTertiaryContainer = Color(0xFF261900)

private val RedDarkPrimary = Color(0xFFef9a9a)
private val RedDarkOnPrimary = Color(0xFF690005)
private val RedDarkPrimaryContainer = Color(0xFF930006)
private val RedDarkOnPrimaryContainer = Color(0xFFffdad4)
private val RedDarkSecondary = Color(0xFFe7bdb6)
private val RedDarkOnSecondary = Color(0xFF442925)
private val RedDarkSecondaryContainer = Color(0xFF5d3f3a)
private val RedDarkOnSecondaryContainer = Color(0xFFffdad4)
private val RedDarkTertiary = Color(0xFFdbc06c)
private val RedDarkOnTertiary = Color(0xFF3d2e00)
private val RedDarkTertiaryContainer = Color(0xFF584417)
private val RedDarkOnTertiaryContainer = Color(0xFFfbdfa6)

/**
 * Builds a full [ColorScheme] for the given [accentColor] and theme mode.
 * Non-accent colors (background, surface, error, slate variants) remain unchanged.
 */
fun accentColorScheme(accentColor: AccentColor, darkTheme: Boolean): ColorScheme = when {
    darkTheme -> when (accentColor) {
        AccentColor.Teal -> darkColorScheme(
            primary = Primary,
            onPrimary = OnPrimary,
            primaryContainer = TealDarkPrimaryContainer,
            onPrimaryContainer = TealDarkOnPrimaryContainer,
            secondary = TealDarkSecondary,
            onSecondary = TealDarkOnSecondary,
            secondaryContainer = TealDarkSecondaryContainer,
            onSecondaryContainer = TealDarkOnSecondaryContainer,
            tertiary = TealDarkTertiary,
            onTertiary = TealDarkOnTertiary,
            tertiaryContainer = TealDarkTertiaryContainer,
            onTertiaryContainer = TealDarkOnTertiaryContainer,
            background = BackgroundDark,
            surface = BackgroundDark,
            onBackground = OnBackgroundDark,
            onSurface = OnBackgroundDark,
            onSurfaceVariant = Slate400,
            outline = Slate700,
            surfaceVariant = Slate800,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer,
        )
        AccentColor.Blue -> darkColorScheme(
            primary = BlueDarkPrimary,
            onPrimary = BlueDarkOnPrimary,
            primaryContainer = BlueDarkPrimaryContainer,
            onPrimaryContainer = BlueDarkOnPrimaryContainer,
            secondary = BlueDarkSecondary,
            onSecondary = BlueDarkOnSecondary,
            secondaryContainer = BlueDarkSecondaryContainer,
            onSecondaryContainer = BlueDarkOnSecondaryContainer,
            tertiary = BlueDarkTertiary,
            onTertiary = BlueDarkOnTertiary,
            tertiaryContainer = BlueDarkTertiaryContainer,
            onTertiaryContainer = BlueDarkOnTertiaryContainer,
            background = BackgroundDark,
            surface = BackgroundDark,
            onBackground = OnBackgroundDark,
            onSurface = OnBackgroundDark,
            onSurfaceVariant = Slate400,
            outline = Slate700,
            surfaceVariant = Slate800,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer,
        )
        AccentColor.Red -> darkColorScheme(
            primary = RedDarkPrimary,
            onPrimary = RedDarkOnPrimary,
            primaryContainer = RedDarkPrimaryContainer,
            onPrimaryContainer = RedDarkOnPrimaryContainer,
            secondary = RedDarkSecondary,
            onSecondary = RedDarkOnSecondary,
            secondaryContainer = RedDarkSecondaryContainer,
            onSecondaryContainer = RedDarkOnSecondaryContainer,
            tertiary = RedDarkTertiary,
            onTertiary = RedDarkOnTertiary,
            tertiaryContainer = RedDarkTertiaryContainer,
            onTertiaryContainer = RedDarkOnTertiaryContainer,
            background = BackgroundDark,
            surface = BackgroundDark,
            onBackground = OnBackgroundDark,
            onSurface = OnBackgroundDark,
            onSurfaceVariant = Slate400,
            outline = Slate700,
            surfaceVariant = Slate800,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer,
        )
    }
    else -> when (accentColor) {
        AccentColor.Teal -> lightColorScheme(
            primary = PrimaryLight,
            onPrimary = OnPrimary,
            primaryContainer = TealLightPrimaryContainer,
            onPrimaryContainer = TealLightOnPrimaryContainer,
            secondary = TealLightSecondary,
            onSecondary = TealLightOnSecondary,
            secondaryContainer = TealLightSecondaryContainer,
            onSecondaryContainer = TealLightOnSecondaryContainer,
            tertiary = TealLightTertiary,
            onTertiary = TealLightOnTertiary,
            tertiaryContainer = TealLightTertiaryContainer,
            onTertiaryContainer = TealLightOnTertiaryContainer,
            background = BackgroundLight,
            surface = BackgroundLight,
            surfaceContainer = Color(0xFFF0F1F3),
            surfaceContainerHigh = Color(0xFFE8E9EB),
            onBackground = OnBackgroundLight,
            onSurface = OnBackgroundLight,
            onSurfaceVariant = Slate600,
            outline = Slate300,
            surfaceVariant = Slate200,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer,
        )
        AccentColor.Blue -> lightColorScheme(
            primary = BlueLightPrimary,
            onPrimary = BlueLightOnPrimary,
            primaryContainer = BlueLightPrimaryContainer,
            onPrimaryContainer = BlueLightOnPrimaryContainer,
            secondary = BlueLightSecondary,
            onSecondary = BlueLightOnSecondary,
            secondaryContainer = BlueLightSecondaryContainer,
            onSecondaryContainer = BlueLightOnSecondaryContainer,
            tertiary = BlueLightTertiary,
            onTertiary = BlueLightOnTertiary,
            tertiaryContainer = BlueLightTertiaryContainer,
            onTertiaryContainer = BlueLightOnTertiaryContainer,
            background = BackgroundLight,
            surface = BackgroundLight,
            surfaceContainer = Color(0xFFF0F1F3),
            surfaceContainerHigh = Color(0xFFE8E9EB),
            onBackground = OnBackgroundLight,
            onSurface = OnBackgroundLight,
            onSurfaceVariant = Slate600,
            outline = Slate300,
            surfaceVariant = Slate200,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer,
        )
        AccentColor.Red -> lightColorScheme(
            primary = RedLightPrimary,
            onPrimary = RedLightOnPrimary,
            primaryContainer = RedLightPrimaryContainer,
            onPrimaryContainer = RedLightOnPrimaryContainer,
            secondary = RedLightSecondary,
            onSecondary = RedLightOnSecondary,
            secondaryContainer = RedLightSecondaryContainer,
            onSecondaryContainer = RedLightOnSecondaryContainer,
            tertiary = RedLightTertiary,
            onTertiary = RedLightOnTertiary,
            tertiaryContainer = RedLightTertiaryContainer,
            onTertiaryContainer = RedLightOnTertiaryContainer,
            background = BackgroundLight,
            surface = BackgroundLight,
            surfaceContainer = Color(0xFFF0F1F3),
            surfaceContainerHigh = Color(0xFFE8E9EB),
            onBackground = OnBackgroundLight,
            onSurface = OnBackgroundLight,
            onSurfaceVariant = Slate600,
            outline = Slate300,
            surfaceVariant = Slate200,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer,
        )
    }
}
