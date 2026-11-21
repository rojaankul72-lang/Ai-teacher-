package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = OnSaffronContainer,
    secondary = PeacockTeal,
    onSecondary = Color.White,
    secondaryContainer = PeacockContainer,
    onSecondaryContainer = OnPeacockContainer,
    tertiary = TurmericGold,
    onTertiary = Color.White,
    tertiaryContainer = TurmericContainer,
    onTertiaryContainer = OnTurmericContainer,
    background = ParchmentBackground,
    onBackground = CharcoalInk,
    surface = ParchmentSurface,
    onSurface = CharcoalInk,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = MutedSlate,
    outline = SandstoneOutline,
    error = GentleCoralError,
    errorContainer = GentleCoralContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronDarkPrimary,
    onPrimary = Color(0xFF561B02),
    primaryContainer = SaffronDarkContainer,
    onPrimaryContainer = Color(0xFFFFDFD3),
    secondary = PeacockDarkSecondary,
    onSecondary = Color(0xFF003544),
    secondaryContainer = PeacockDarkContainer,
    onSecondaryContainer = Color(0xFFCFF4FC),
    tertiary = TurmericDarkTertiary,
    onTertiary = Color(0xFF422300),
    tertiaryContainer = TurmericDarkContainer,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = NightStudyBg,
    onBackground = NightCreamInk,
    surface = NightStudySurface,
    onSurface = NightCreamInk,
    surfaceVariant = NightStudyCard,
    onSurfaceVariant = NightMutedInk,
    outline = Color(0xFF4A3F37)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BharatShikshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
