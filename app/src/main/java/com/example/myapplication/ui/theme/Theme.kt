package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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