package app.rednote_m25.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RednoteRed = Color(0xFFFF2442)
private val RednotePink = Color(0xFFFF6B6B)
private val RednoteOrange = Color(0xFFFF9F43)
private val RednoteYellow = Color(0xFFFECA57)
private val RednoteGreen = Color(0xFF1DD1A1)
private val RednoteBlue = Color(0xFF54A0FF)
private val RednotePurple = Color(0xFF9B59B6)

private val LightColorScheme = lightColorScheme(
    primary = RednoteRed,
    onPrimary = Color.White,
    primaryContainer = RednotePink.copy(alpha = 0.1f),
    onPrimaryContainer = RednoteRed,
    secondary = RednoteOrange,
    onSecondary = Color.White,
    secondaryContainer = RednoteOrange.copy(alpha = 0.1f),
    onSecondaryContainer = RednoteOrange,
    tertiary = RednoteBlue,
    onTertiary = Color.White,
    tertiaryContainer = RednoteBlue.copy(alpha = 0.1f),
    onTertiaryContainer = RednoteBlue,
    background = Color(0xFFFFFBFB),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = RednoteRed,
    onPrimary = Color.White,
    primaryContainer = RednoteRed.copy(alpha = 0.2f),
    onPrimaryContainer = RednotePink,
    secondary = RednoteOrange,
    onSecondary = Color.Black,
    secondaryContainer = RednoteOrange.copy(alpha = 0.2f),
    onSecondaryContainer = RednoteOrange,
    tertiary = RednoteBlue,
    onTertiary = Color.Black,
    tertiaryContainer = RednoteBlue.copy(alpha = 0.2f),
    onTertiaryContainer = RednoteBlue,
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFFFFBFB),
    surface = Color(0xFF2D2D2D),
    onSurface = Color(0xFFFFFBFB),
    surfaceVariant = Color(0xFF3D3D3D),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF4D4D4D)
)

@Composable
fun RednoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
