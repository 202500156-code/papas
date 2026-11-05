package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun BuscadorScreen(
    onSeleccionarNovela: (Novela) -> Unit = {}
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Todos") }

    val generos = listOf("Todos", "Misterio", "Fantasía", "Romance", "Terror", "Ciencia Ficción")

    val catálogoBiblioteca = remember {
        listOf(
            Novela(
                id = "1",
                titulo = "La casa de la marea",
                autor = "Elena Mar",
                categoria = "Misterio",
                sinopsis = "Hay secretos que el mar siempre devuelve."
            ),
            Novela(
                id = "2",
                titulo = "El atlas de las horas",
                autor = "Inés Vidal",
                categoria = "Fantasía",
                sinopsis = "Un mapa antiguo que detiene el tiempo."
            ),
            Novela(
                id = "3",
                titulo = "Donde duermen los árboles",
                autor = "Mateo Ríos",
                categoria = "Romance",
                sinopsis = "Las historias olvidadas del bosque."
            ),
            Novela(
                id = "4",
                titulo = "La ciudad de los espejos",
                autor = "Carlos Ruiz",
                categoria = "Misterio",
                sinopsis = "Nadie sale de una ciudad construida de reflejos."
            )
        )
    }

    val resultadosFiltrados = catálogoBiblioteca.filter { novela ->
        val coincideBusqueda = novela.titulo.contains(textoBusqueda, ignoreCase = true) ||
                novela.autor.contains(textoBusqueda, ignoreCase = true)
        val coincideGenero = if (generoSeleccionado == "Todos") true else novela.categoria.equals(generoSeleccionado, ignoreCase = true)

        coincideBusqueda && coincideGenero
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Biblioteca y Búsqueda",
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary
        )
        Text(
            text = "Explora el catálogo completo y tus lecturas guardadas",
            fontSize = 13.sp,
            color = ReadwoodDarkTextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            placeholder = { Text("Buscar por título, autor o palabra clave...", color = ReadwoodDarkTextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = ReadwoodDarkTextMuted)
            },
            trailingIcon = {
                if (textoBusqueda.isNotEmpty()) {
                    IconButton(onClick = { textoBusqueda = "" }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Limpiar", tint = ReadwoodDarkTextMuted)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(generos) { gen ->
                val esSeleccionado = generoSeleccionado == gen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (esSeleccionado) ReadwoodDarkPrimary else ReadwoodDarkSurface,
                    border = if (esSeleccionado) null else BorderStroke(1.dp, ReadwoodDarkBorder),
                    modifier = Modifier.clickable { generoSeleccionado = gen }
                ) {
                    Text(
                        text = gen,
                        fontSize = 12.sp,
                        color = if (esSeleccionado) Color.White else ReadwoodDarkTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (resultadosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron obras con ese criterio.",
                    color = ReadwoodDarkTextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            resultadosFiltrados.forEach { novela ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onSeleccionarNovela(novela) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 50.dp, height = 75.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ReadwoodDarkPrimary)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = novela.titulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ReadwoodDarkTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${novela.autor} · ${novela.categoria}",
                                fontSize = 12.sp,
                                color = ReadwoodDarkTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = novela.sinopsis,
                                fontSize = 11.sp,
                                color = ReadwoodDarkTextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}