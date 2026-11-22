package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ReadwoodColorPalette(
    val bg: Color,
    val cardBg: Color,
    val purple: Color,
    val textDark: Color,
    val textMuted: Color,
    val border: Color
)

val LightReadwoodPalette = ReadwoodColorPalette(
    bg = Color(0xFFF7F3EB),       // Papel crema cálido
    cardBg = Color(0xFFEFE9DD),   // Lino suave
    purple = Color(0xFF5E2D40),   // Borgoña editorial
    textDark = Color(0xFF1F1C1A), // Tinta carbón
    textMuted = Color(0xFF6B625B),// Gris piedra cálido
    border = Color(0xFFD8CFC2)    // Filete de papel
)

val DarkReadwoodPalette = ReadwoodColorPalette(
    bg = Color(0xFF161413),       // Tinta nocturna
    cardBg = Color(0xFF211E1C),   // Carbón suave
    purple = Color(0xFFD48CA6),   // Rosa antiguo legible
    textDark = Color(0xFFEDE7DF), // Papel marfil
    textMuted = Color(0xFF9E958C),// Ceniza cálida
    border = Color(0xFF332E2B)    // Borde sutil
)

val LocalReadwoodColors = staticCompositionLocalOf { LightReadwoodPalette }

@Composable
fun ReadwoodTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkReadwoodPalette else LightReadwoodPalette
    val materialScheme = if (darkTheme) {
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
        MaterialTheme(
            colorScheme = materialScheme,
            content = content
        )
    }
}