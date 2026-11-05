package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LecturaScreen(
    tituloNovela: String,
    genero: String = "Clásico",
    tituloCapitulo: String = "Capítulo 1",
    urlTexto: String? = null,
    onVolver: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Estado para el tema visual de lectura
    var temaActual by remember { mutableStateOf(ConfigTemas.Sepia) }
    var contenidoTexto by remember { mutableStateOf("Cargando obra completa desde la biblioteca...") }
    var cargando by remember { mutableStateOf(true) }

    // Descarga del texto original en segundo plano desde la API
    LaunchedEffect(urlTexto) {
        if (!urlTexto.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(urlTexto)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000

                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        val rawText = conn.inputStream.bufferedReader().use { it.readText() }
                        // Limpieza básica de texto e instrucciones del Gutenberg Project
                        val textoLimpio = rawText
                            .replace(Regex("<[^>]*>"), "") // Elimina tags HTML si los hay
                            .replace(Regex("\r\n|\r"), "\n")
                            .take(15000) // Carga los primeros 15,000 caracteres para rendimiento

                        contenidoTexto = if (textoLimpio.isNotBlank()) textoLimpio else "El contenido de esta obra no está disponible en formato plano."
                    } else {
                        contenidoTexto = "No se pudo conectar con el servidor de la obra."
                    }
                } catch (e: Exception) {
                    contenidoTexto = "Había una vez en los confines del reino de Readwood, donde los tomos antiguos guardaban secretos olvidados por el tiempo..."
                } finally {
                    cargando = false
                }
            }
        } else {
            contenidoTexto = "Había una vez en los confines del reino de Readwood, donde los tomos antiguos guardaban secretos olvidados por el tiempo..."
            cargando = false
        }
    }

    val porcentajeLectura = if (scrollState.maxValue > 0) {
        ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt()
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(temaActual.fondo)
    ) {
        // Barra superior con controles y porcentaje
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) {
                Text("← Volver", color = temaActual.texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Selector de temas de lectura en vivo
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ConfigTemas.Sepia, ConfigTemas.Noche, ConfigTemas.Mistico, ConfigTemas.Claro).forEach { tema ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(tema.fondo)
                            .clickable { temaActual = tema }
                    )
                }
            }

            Text(
                text = "$porcentajeLectura%",
                color = temaActual.acento,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Barra de avance de lectura
        LinearProgressIndicator(
            progress = { if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = temaActual.acento,
            trackColor = temaActual.texto.copy(alpha = 0.1f)
        )

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = temaActual.acento)
            }
        } else {
            // Cuerpo de lectura decorado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Encabezado ornamental
                EncabezadoOrnamental(
                    genero = genero,
                    tituloNovela = tituloNovela,
                    tituloCapitulo = tituloCapitulo,
                    colorAcento = temaActual.acento,
                    colorTexto = temaActual.texto
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Texto con letra capitular
                TextoCapitularDecorado(
                    contenido = contenidoTexto,
                    colorAcento = temaActual.acento,
                    colorTexto = temaActual.texto,
                    tamanoTexto = 17f
                )
            }
        }
    }
}