package com.example.myapplication

import androidx.compose.ui.graphics.Color

data class TemaLectura(
    val nombre: String,
    val fondo: Color,
    val texto: Color,
    val acento: Color
)

object ConfigTemas {
    val Sepia = TemaLectura(
        nombre = "Sepia",
        fondo = Color(0xFFF4ECD8),
        texto = Color(0xFF2B2B2B),
        acento = Color(0xFF8B4513)
    )
    val Noche = TemaLectura(
        nombre = "Noche",
        fondo = Color(0xFF121212),
        texto = Color(0xFFE0E0E0),
        acento = Color(0xFFBB86FC)
    )
    val Mistico = TemaLectura(
        nombre = "Místico",
        fondo = Color(0xFF1A1A2E),
        texto = Color(0xFFE2E2E2),
        acento = Color(0xFF00ADB5)
    )
    val Claro = TemaLectura(
        nombre = "Claro",
        fondo = Color.White,
        texto = Color(0xFF1C1C1C),
        acento = Color(0xFF6200EE)
    )
}