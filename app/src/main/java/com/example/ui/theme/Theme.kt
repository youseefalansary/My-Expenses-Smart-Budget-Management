package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

// 1. Dark Slate ColorScheme
private val DarkSlateColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = NeonCyan,
    secondary = NeonEmerald,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = NeonEmeraldLight,
    tertiary = NeonAmber,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    outlineVariant = Slate800,
    error = NeonCoral,
    onError = Slate50
)

// 2. Pure Clean Light ColorScheme (ثيم أبيض فاتح ناصع)
private val PureLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightSecondary,
    tertiary = LightTertiary,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = LightSurface
)

// 3. Emerald & Gold ColorScheme (الزمردي والذهبي الملكي)
private val EmeraldGoldColorScheme = darkColorScheme(
    primary = EmeraldNeon,
    onPrimary = EmeraldForestDark,
    primaryContainer = EmeraldSurfaceVariant,
    onPrimaryContainer = EmeraldNeon,
    secondary = EmeraldGold,
    onSecondary = EmeraldForestDark,
    secondaryContainer = EmeraldSurfaceVariant,
    onSecondaryContainer = EmeraldGold,
    tertiary = EmeraldGold,
    onTertiary = EmeraldForestDark,
    background = EmeraldForestDark,
    onBackground = EmeraldLightText,
    surface = EmeraldSurface,
    onSurface = EmeraldLightText,
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = EmeraldMutedText,
    outline = EmeraldBorder,
    outlineVariant = EmeraldBorder,
    error = NeonCoral,
    onError = Slate50
)

// 4. Midnight OLED ColorScheme (سواد مطلق 100%)
private val MidnightOledColorScheme = darkColorScheme(
    primary = OledViolet,
    onPrimary = OledBlack,
    primaryContainer = OledSurfaceVariant,
    onPrimaryContainer = OledViolet,
    secondary = OledCyan,
    onSecondary = OledBlack,
    secondaryContainer = OledSurfaceVariant,
    onSecondaryContainer = OledCyan,
    tertiary = OledPink,
    onTertiary = OledBlack,
    background = OledBlack,
    onBackground = OledWhite,
    surface = OledSurface,
    onSurface = OledWhite,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = OledMuted,
    outline = OledBorder,
    outlineVariant = OledBorder,
    error = OledPink,
    onError = OledWhite
)

fun getMasrofiShapes(radiusDp: Int): Shapes {
    val r = radiusDp.coerceAtLeast(2)
    val sm = (r * 0.5).toInt().coerceAtLeast(2)
    val md = (r * 0.75).toInt().coerceAtLeast(3)
    val xl = (r * 1.3).toInt().coerceAtLeast(4)

    return Shapes(
        small = RoundedCornerShape(sm.dp),
        medium = RoundedCornerShape(md.dp),
        large = RoundedCornerShape(r.dp),
        extraLarge = RoundedCornerShape(xl.dp)
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.DARK_SLATE,
    cornerStyle: CornerStyle = CornerStyle.CLASSIC,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DARK_SLATE -> DarkSlateColorScheme
        ThemeMode.PURE_LIGHT -> PureLightColorScheme
        ThemeMode.EMERALD_GOLD -> EmeraldGoldColorScheme
        ThemeMode.MIDNIGHT_OLED -> MidnightOledColorScheme
    }

    val shapes = getMasrofiShapes(cornerStyle.radiusDp)

    CompositionLocalProvider(
        LocalAppCornerRadius provides cornerStyle.radiusDp.dp,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content
        )
    }
}
