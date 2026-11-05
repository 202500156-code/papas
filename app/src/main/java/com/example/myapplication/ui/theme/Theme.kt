package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Colores globales de Readwood ---
val ReadwoodDarkBackground = Color(0xFF121212)
val ReadwoodDarkSurface = Color(0xFF1E1E1E)
val ReadwoodDarkPrimary = Color(0xFFD4A373)
val ReadwoodDarkButton = Color(0xFF333333)
val ReadwoodDarkBorder = Color(0xFF2C2C2C)
val ReadwoodDarkTextPrimary = Color(0xFFEEEEEE)
val ReadwoodDarkTextMuted = Color(0xFFA0A0A0)

val ReadwoodBg = Color(0xFFF8F3ED)
val ReadwoodPurple = Color(0xFF6B3A50)
val ReadwoodCardBg = Color(0xFFF0EAE1)
val ReadwoodTextDark = Color(0xFF2D2329)
val ReadwoodTextMuted = Color(0xFF8A7E85)
val ReadwoodBorder = Color(0xFFE2DCD5)

private val ReadwoodColorScheme = darkColorScheme(
    background = ReadwoodDarkBackground,
    surface = ReadwoodDarkSurface,
    primary = ReadwoodDarkPrimary,
    onBackground = ReadwoodDarkTextPrimary,
    onSurface = ReadwoodDarkTextPrimary
)

@Composable
fun ReadwoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReadwoodColorScheme,
        content = content
    )
}