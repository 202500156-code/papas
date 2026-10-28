package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReadwoodColorScheme = darkColorScheme(
    background = ReadwoodDarkBackground,
    surface = ReadwoodDarkSurface,
    primary = ReadwoodDarkPrimary,
    onBackground = ReadwoodDarkTextPrimary,
    onSurface = ReadwoodDarkTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReadwoodColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias para que MainActivity reconozca ReadwoodTheme
@Composable
fun ReadwoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        content = content
    )
}