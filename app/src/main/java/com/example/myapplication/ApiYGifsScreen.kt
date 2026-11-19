package com.example.myapplication

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ApiYGifsScreen(onVolver: () -> Unit) {
    val colors = LocalReadwoodColors.current
    var tabIndex by remember { mutableIntStateOf(0) } // 0: Página de GIFs animados, 1: Llamadas a API
    var urlEndpoint by remember { mutableStateOf("https://jsonplaceholder.typicode.com/posts?_limit=5") }
    var resultadosApi by remember { mutableStateOf<List<String>>(emptyList()) }
    var cargandoApi by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val gifsReacciones = listOf(
        "Pluma Escribiendo" to Color(0xFF6B3A50),
        "Página Pasando" to Color(0xFF8C536C),
        "Taza de Café Humeante" to Color(0xFFA66B4E),
        "Vela Nocturna" to Color(0xFFD49A5A),
        "Aplauso Literario" to Color(0xFF4F6D7A),
        "Corazón Lector" to Color(0xFFB83B5E)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onVolver) { Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold) }
            Text("GIFs y Llamadas API", fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
            Spacer(modifier = Modifier.width(60.dp))
        }

        TabRow(selectedTabIndex = tabIndex, containerColor = colors.cardBg, contentColor = colors.purple) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Página de GIFs") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Llamada a APIs") })
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (tabIndex == 0) {
            // Galería de GIFs / Animaciones en bucle continuo
            val infiniteTransition = rememberInfiniteTransition(label = "gifLoop")
            val pulso by infiniteTransition.animateFloat(
                initialValue = 0.65f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "pulso"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gifsReacciones) { (tituloGif, tono) ->
                    Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), shape = RoundedCornerShape(12.dp)) {
                        Column(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawCircle(
                                    color = tono.copy(alpha = pulso),
                                    radius = (size.minDimension / 2.4f) * pulso,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(tituloGif, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                            Text("GIF Animado", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                }
            }
        } else {
            // Módulo aislado de llamada HTTP GET a APIs
            OutlinedTextField(
                value = urlEndpoint,
                onValueChange = { urlEndpoint = it },
                label = { Text("Endpoint URL (GET)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    cargandoApi = true
                    scope.launch(Dispatchers.IO) {
                        val lista = mutableListOf<String>()
                        try {
                            val conn = (URL(urlEndpoint).openConnection() as HttpURLConnection).apply {
                                requestMethod = "GET"
                                connectTimeout = 8000
                                readTimeout = 8000
                            }
                            val raw = conn.inputStream.bufferedReader().use { it.readText() }
                            val arr = JSONArray(raw)
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                lista.add("#${obj.optInt("id")}: ${obj.optString("title", obj.toString().take(80))}")
                            }
                        } catch (e: Exception) {
                            lista.add("Error en llamada API: ${e.message}")
                        }
                        withContext(Dispatchers.Main) {
                            resultadosApi = lista
                            cargandoApi = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ejecutar Llamada API", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            if (cargandoApi) {
                CircularProgressIndicator(color = colors.purple)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(resultadosApi) { linea ->
                        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                            Text(linea, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = colors.textDark)
                        }
                    }
                }
            }
        }
    }
}