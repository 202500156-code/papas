package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun LecturaScreen(tituloNovela: String, onVolver: () -> Unit) {
    val scrollState = rememberScrollState()

    // Cálculo en tiempo real del porcentaje de lectura
    val porcentajeLectura = if (scrollState.maxValue > 0) {
        ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
    ) {
        // Encabezado superior con botón Volver y Porcentaje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReadwoodDarkSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton)
            ) {
                Text("← Volver")
            }
            Text(
                text = "$porcentajeLectura% leído",
                color = ReadwoodDarkPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Indicador visual de progreso
        LinearProgressIndicator(
            progress = { if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f },
            modifier = Modifier.fillMaxWidth(),
            color = ReadwoodDarkPrimary,
            trackColor = ReadwoodDarkBorder,
        )

        // Contenido del Capítulo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = tituloNovela,
                fontSize = 26.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = ReadwoodDarkTextPrimary
            )
            Text(
                text = "Capítulo 1",
                fontSize = 16.sp,
                color = ReadwoodDarkTextMuted,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Aquí va todo el cuerpo de la historia... ".repeat(100),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = ReadwoodDarkTextPrimary
            )
        }
    }
}