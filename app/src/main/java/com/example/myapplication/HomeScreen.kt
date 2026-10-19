package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colorimetría exacta de Readwood (Figma)
val ReadwoodBg = Color(0xFFF8F3ED)
val ReadwoodPurple = Color(0xFF6B3A50)
val ReadwoodCardBg = Color(0xFFF0EAE1)
val ReadwoodTextDark = Color(0xFF2D2329)
val ReadwoodTextMuted = Color(0xFF8A7E85)
val ReadwoodBorder = Color(0xFFE2DCD5)

// Modelo local para rendering inmediato sin fallos de red
data class NovelaItem(
    val id: String,
    val titulo: String,
    val autor: String,
    val genero: String,
    val descripcion: String,
    val capituloActual: String = "",
    val progreso: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAbrirNotificaciones: () -> Unit = {},
    onSeleccionarNovela: (NovelaItem) -> Unit = {}
) {
    // Datos locales fijos basados en las capturas de UI xd
    val listaNovelas = remember {
        listOf(
            NovelaItem("1", "La casa de la marea", "Elena Mar", "Misterio", "Hay secretos que el mar siempre devuelve.", "Cap. 3 - La llave de sal", 0.25f),
            NovelaItem("2", "El atlas de las horas", "Inés Vidal", "Fantasía", "Un mapa antiguo que detiene el tiempo.", "Cap. 8", 0.40f),
            NovelaItem("3", "Donde duermen los árboles", "Mateo Ríos", "Romance", "Las historias olvidadas del bosque.", "Cap. 18", 1.0f)
        )
    }

    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Para ti") }

    val generos = listOf("Para ti", "Misterio", "Fantasía", "Romance")

    // Filtrado en tiempo real por búsqueda (por título o por autor) y por género
    val novelasFiltradas = listaNovelas.filter { novela ->
        val coincideBusqueda = novela.titulo.contains(textoBusqueda, ignoreCase = true) ||
                novela.autor.contains(textoBusqueda, ignoreCase = true)

        val coincideGenero = if (generoSeleccionado == "Para ti") {
            true
        } else {
            novela.genero.equals(generoSeleccionado, ignoreCase = true)
        }

        coincideBusqueda && coincideGenero
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Encabezado superior con marca y campana de notificaciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Readwood", //Titulo de la app
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReadwoodPurple
                )
                Text(
                    text = "HISTORIAS QUE HABITAR",
                    fontSize = 9.sp,
                    color = ReadwoodTextMuted,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = onAbrirNotificaciones) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = ReadwoodTextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saludo personalizado para proximo avance, recordar no hacerlo al ultimo
        Text(
            text = "Hola mundo papu", //es un texto cualquiera
            fontSize = 11.sp,
            color = ReadwoodTextMuted,
            letterSpacing = 1.sp
        )
        Text(
            text = "Tu próxima historia te\nestá esperando.",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // arra de Búsqueda (Filtra por Autor o Novela)
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            placeholder = {
                Text("Busca novelas, autores o mundos", fontSize = 13.sp, color = ReadwoodTextMuted)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = ReadwoodTextMuted)
            },
            trailingIcon = {
                if (textoBusqueda.isNotEmpty()) {
                    IconButton(onClick = { textoBusqueda = "" }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Limpiar", tint = ReadwoodTextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = ReadwoodBorder,
                focusedBorderColor = ReadwoodPurple
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Chips de Géneros
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(generos) { gen ->
                val esSeleccionado = generoSeleccionado == gen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (esSeleccionado) ReadwoodPurple else Color.Transparent,
                    border = if (esSeleccionado) null else androidx.compose.foundation.BorderStroke(1.dp, ReadwoodBorder),
                    modifier = Modifier.clickable { generoSeleccionado = gen }
                ) {
                    Text(
                        text = gen,
                        fontSize = 12.sp,
                        color = if (esSeleccionado) Color.White else ReadwoodTextDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Novela Destacada / Recomendada ("Para ti")
        if (novelasFiltradas.isNotEmpty()) {
            val recomendada = novelasFiltradas.first()
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ReadwoodCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeleccionarNovela(recomendada) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier //esta es una imagen
                            .size(width = 75.dp, height = 110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ReadwoodPurple)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("ELECCIÓN DE FOLIO", fontSize = 10.sp, color = ReadwoodPurple, fontWeight = FontWeight.Bold)
                        Text(recomendada.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReadwoodTextDark)
                        Text("${recomendada.autor} · ${recomendada.genero}", fontSize = 12.sp, color = ReadwoodTextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(recomendada.descripcion, fontSize = 12.sp, color = ReadwoodTextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Descubrir novela →", color = ReadwoodPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            Text("No se encontraron novelas o autores.", color = ReadwoodTextMuted, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Lista de "Mundos para perderse" (Resultados según filtro/búsqueda)
        Text("Mundos para perderse", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReadwoodTextDark)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            novelasFiltradas.take(2).forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSeleccionarNovela(item) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ReadwoodPurple)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ReadwoodTextDark)
                    Text(item.autor, fontSize = 12.sp, color = ReadwoodTextMuted)
                    Text(item.genero, fontSize = 11.sp, color = ReadwoodTextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección "Vuelve a tu lectura"
        Text("Vuelve a tu lectura", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ReadwoodTextDark)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 45.dp, height = 65.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ReadwoodPurple)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("La casa de la marea", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ReadwoodTextDark)
                    Text("Elena Mar · Cap. 3 - La llave de sal", fontSize = 11.sp, color = ReadwoodTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 0.25f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ReadwoodPurple,
                        trackColor = ReadwoodBorder
                    )
                }
            }
        }
    }
}