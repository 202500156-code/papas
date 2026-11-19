package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ReadwoodPalette(
    val bg: Color,
    val cardBg: Color,
    val purple: Color,
    val textDark: Color,
    val textMuted: Color,
    val border: Color,
    val isDark: Boolean
)

val LightReadwoodPalette = ReadwoodPalette(
    bg = Color(0xFFF8F3ED),
    cardBg = Color(0xFFF0EAE1),
    purple = Color(0xFF6B3A50),
    textDark = Color(0xFF2D2329),
    textMuted = Color(0xFF8A7E85),
    border = Color(0xFFE0D7CD),
    isDark = false
)

val DarkReadwoodPalette = ReadwoodPalette(
    bg = Color(0xFF161215),
    cardBg = Color(0xFF231D21),
    purple = Color(0xFFC47D9B),
    textDark = Color(0xFFF4EDE6),
    textMuted = Color(0xFFA3989E),
    border = Color(0xFF382F35),
    isDark = true
)

val LocalReadwoodColors = staticCompositionLocalOf { LightReadwoodPalette }

@Composable
fun ReadwoodTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkReadwoodPalette else LightReadwoodPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.purple,
            background = palette.bg,
            surface = palette.cardBg,
            onBackground = palette.textDark,
            onSurface = palette.textDark
        )
    } else {
        lightColorScheme(
            primary = palette.purple,
            background = palette.bg,
            surface = palette.cardBg,
            onBackground = palette.textDark,
            onSurface = palette.textDark
        )
    }

    CompositionLocalProvider(LocalReadwoodColors provides palette) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}