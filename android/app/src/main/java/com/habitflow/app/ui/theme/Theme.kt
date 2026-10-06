package com.habitflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KineticDark = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF1000A9),
    primaryContainer = Color(0xFF8083FF),
    onPrimaryContainer = Color(0xFF0D0096),
    secondary = Color(0xFF4EDEA3),
    onSecondary = Color(0xFF003824),
    tertiary = Color(0xFFFFB95F),
    onTertiary = Color(0xFF472A00),
    background = Color(0xFF0B1326),
    onBackground = Color(0xFFDAE2FD),
    surface = Color(0xFF171F33),
    onSurface = Color(0xFFDAE2FD),
    surfaceVariant = Color(0xFF222A3D),
    onSurfaceVariant = Color(0xFFC7C4D7),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF464554),
)

@Composable
fun HabitFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KineticDark, content = content)
}
