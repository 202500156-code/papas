package com.example.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun PerfilScreen(
    contador: Int,
    velocidadMultiplicador: Float,
    onCambiarVelocidad: (Float) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var ocultarSeguidos by remember { mutableStateOf(false) }

    val rotacion = remember { Animatable(0f) }

    LaunchedEffect(velocidadMultiplicador) {
        val duracionVuelta = (2000f / velocidadMultiplicador).toInt().coerceAtLeast(100)
        rotacion.animateTo(
            targetValue = rotacion.value + 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duracionVuelta, easing = LinearEasing)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Indicador de Actividad",
                    color = ReadwoodDarkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Canvas(modifier = Modifier.size(100.dp)) {
                    val anchoOvalo = 70.dp.toPx()
                    val altoOvalo = 35.dp.toPx()

                    rotate(degrees = rotacion.value % 360f, pivot = center) {
                        drawRoundRect(
                            color = ReadwoodDarkPrimary,
                            topLeft = Offset(center.x - (anchoOvalo / 2), center.y - (altoOvalo / 2)),
                            size = Size(anchoOvalo, altoOvalo),
                            cornerRadius = CornerRadius(altoOvalo / 2, altoOvalo / 2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Contador: $contador",
                    color = ReadwoodDarkTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Velocidad actual: ${velocidadMultiplicador.toInt()}x",
                    color = ReadwoodDarkTextPrimary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1f, 2f, 4f, 8f).forEach { v ->
                        FilterChip(
                            selected = velocidadMultiplicador == v,
                            onClick = { onCambiarVelocidad(v) },
                            label = { Text("${v.toInt()}x") }
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Privacidad", color = ReadwoodDarkPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ocultar a quién sigo", color = ReadwoodDarkTextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = ocultarSeguidos,
                        onCheckedChange = { ocultarSeguidos = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cerrar Sesión")
        }
    }
}