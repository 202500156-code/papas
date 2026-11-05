package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAbrirNotificaciones: () -> Unit = {},
    onSeleccionarNovela: (Novela) -> Unit = {}
) {
    var novelas by remember { mutableStateOf<List<Novela>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Todos") }

    val generos = listOf("Todos", "Fantasía", "Romance", "Misterio", "Terror", "Ciencia Ficción")

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val connection = (URL("https://gutendex.com/books/?topic=fiction").openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val jsonTexto = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonRoot = JSONObject(jsonTexto)
                val resultsArray = jsonRoot.getJSONArray("results")

                val listaTemporal = mutableListOf<Novela>()
                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val idString = item.optInt("id", i).toString()
                    val titulo = item.optString("title", "Sin título")

                    val authorsArray = item.optJSONArray("authors")
                    val autor = if (authorsArray != null && authorsArray.length() > 0) {
                        authorsArray.getJSONObject(0).optString("name", "Autor Desconocido")
                    } else {
                        "Autor Desconocido"
                    }

                    val subjectsArray = item.optJSONArray("subjects")
                    val categoria = if (subjectsArray != null && subjectsArray.length() > 0) {
                        subjectsArray.getString(0)
                    } else {
                        "Ficción"
                    }

                    listaTemporal.add(
                        Novela(
                            id = idString,
                            titulo = titulo,
                            autor = autor,
                            sinopsis = "Novela disponible en el catálogo público de Readwood.",
                            categoria = categoria
                        )
                    )
                }
                novelas = listaTemporal
            } catch (e: Exception) {
                errorMsg = "Sin conexión o problema de red"
            } finally {
                cargando = false
            }
        }
    }

    val novelasFiltradas = novelas.filter { novela ->
        val coincideBusqueda = novela.titulo.contains(textoBusqueda, ignoreCase = true) ||
                novela.autor.contains(textoBusqueda, ignoreCase = true)
        val coincideGenero = if (generoSeleccionado == "Todos") true else novela.categoria.contains(generoSeleccionado, ignoreCase = true)

        coincideBusqueda && coincideGenero
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Readwood",
                        fontWeight = FontWeight.Bold,
                        color = ReadwoodPurple,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onAbrirNotificaciones) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = ReadwoodPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReadwoodBg)
            )
        },
        containerColor = ReadwoodBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar en el catálogo...", color = ReadwoodTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = ReadwoodTextMuted)
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Limpiar", tint = ReadwoodTextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ReadwoodCardBg,
                    unfocusedContainerColor = ReadwoodCardBg,
                    focusedBorderColor = ReadwoodPurple,
                    unfocusedBorderColor = ReadwoodBorder,
                    focusedTextColor = ReadwoodTextDark,
                    unfocusedTextColor = ReadwoodTextDark
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(generos) { gen ->
                    val esSeleccionado = generoSeleccionado == gen
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (esSeleccionado) ReadwoodPurple else ReadwoodCardBg,
                        border = if (esSeleccionado) null else BorderStroke(1.dp, ReadwoodBorder),
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

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = ReadwoodPurple)
                } else if (errorMsg != null && novelas.isEmpty()) {
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                } else if (novelasFiltradas.isEmpty()) {
                    Text(
                        text = "No se encontraron obras.",
                        color = ReadwoodTextMuted,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Descubrir Novelas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ReadwoodTextDark,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(novelasFiltradas) { novela ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeleccionarNovela(novela) },
                                colors = CardDefaults.cardColors(containerColor = ReadwoodCardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = novela.titulo,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ReadwoodTextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Autor: ${novela.autor}",
                                        fontSize = 14.sp,
                                        color = ReadwoodTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Categoría: ${novela.categoria}",
                                        fontSize = 12.sp,
                                        color = ReadwoodPurple,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}