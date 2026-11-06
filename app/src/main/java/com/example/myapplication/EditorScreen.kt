package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Modelo de datos para el editor
data class ItemEditor(
    val id: Int,
    var usuario: String,
    var texto: String
)

// Colores privados del tema Readwood
private val ReadwoodBg = Color(0xFFF8F3ED)
private val ReadwoodPurple = Color(0xFF6B3A50)
private val ReadwoodCardBg = Color(0xFFF0EAE1)
private val ReadwoodTextDark = Color(0xFF2D2329)
private val ReadwoodTextMuted = Color(0xFF8A7E85)
private val ReadwoodBorder = Color(0xFFE0D7CD)

@Composable
fun EditorScreen(
    onVolver: () -> Unit = {},
    onPublicarExitoso: () -> Unit = {} // <--- AQUÍ ESTABA LA COLISIÓN: Se agrega el parámetro que faltaba
) {
    val listaItems = remember { mutableStateListOf<ItemEditor>() }

    var usuarioInput by remember { mutableStateOf("") }
    var textoInput by remember { mutableStateOf("") }
    var idEnEdicion by remember { mutableStateOf<Int?>(null) }

    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // 1. GET: Cargar datos iniciales desde la API pública
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val connection = (URL("https://jsonplaceholder.typicode.com/comments?_limit=10").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (connection.responseCode == 200) {
                    val jsonTexto = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonTexto)

                    val datosProcesados = mutableListOf<ItemEditor>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        datosProcesados.add(
                            ItemEditor(
                                id = item.getInt("id"),
                                usuario = item.getString("name").take(15),
                                texto = item.getString("body").take(50)
                            )
                        )
                    }
                    withContext(Dispatchers.Main) {
                        listaItems.clear()
                        listaItems.addAll(datosProcesados)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cargando = false
            }
        }
    }

    // 2. CREAR / EDITAR (Simula POST/PUT en API)
    fun guardarOActualizar() {
        if (usuarioInput.isBlank() || textoInput.isBlank()) return

        val esEdicion = idEnEdicion != null
        val idActual = idEnEdicion ?: ((listaItems.maxOfOrNull { it.id } ?: 0) + 1)
        val userTemp = usuarioInput
        val textTemp = textoInput

        if (esEdicion) {
            val index = listaItems.indexOfFirst { it.id == idActual }
            if (index != -1) {
                listaItems[index] = listaItems[index].copy(usuario = userTemp, texto = textTemp)
            }
        } else {
            listaItems.add(0, ItemEditor(id = idActual, usuario = userTemp, texto = textTemp))
        }

        usuarioInput = ""
        textoInput = ""
        idEnEdicion = null

        // Se invoca el callback para notificar éxito a MainActivity
        onPublicarExitoso()

        scope.launch(Dispatchers.IO) {
            try {
                val urlString = if (esEdicion) "https://jsonplaceholder.typicode.com/comments/$idActual" else "https://jsonplaceholder.typicode.com/comments"
                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = if (esEdicion) "PUT" else "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                val body = JSONObject().apply {
                    put("name", userTemp)
                    put("body", textTemp)
                }

                connection.outputStream.use { it.write(body.toString().toByteArray()) }
                val code = connection.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 3. ELIMINAR (DELETE)
    fun eliminarItem(id: Int) {
        listaItems.removeAll { it.id == id }

        scope.launch(Dispatchers.IO) {
            try {
                val connection = (URL("https://jsonplaceholder.typicode.com/comments/$id").openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                }
                val code = connection.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodBg)
            .padding(20.dp)
    ) {
        Text(
            text = "Editor de Registro",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark
        )
        Text(
            text = "Modifica y gestiona las entradas de la API pública",
            fontSize = 12.sp,
            color = ReadwoodTextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (idEnEdicion == null) "Nueva Entrada" else "Editando ID #$idEnEdicion",
                    fontWeight = FontWeight.Bold,
                    color = ReadwoodPurple,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = usuarioInput,
                    onValueChange = { usuarioInput = it },
                    label = { Text("Usuario (user)", color = ReadwoodTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ReadwoodPurple,
                        unfocusedBorderColor = ReadwoodBorder,
                        focusedTextColor = ReadwoodTextDark,
                        unfocusedTextColor = ReadwoodTextDark
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = textoInput,
                    onValueChange = { textoInput = it },
                    label = { Text("Texto (text)", color = ReadwoodTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ReadwoodPurple,
                        unfocusedBorderColor = ReadwoodBorder,
                        focusedTextColor = ReadwoodTextDark,
                        unfocusedTextColor = ReadwoodTextDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (idEnEdicion != null) {
                        TextButton(onClick = {
                            idEnEdicion = null
                            usuarioInput = ""
                            textoInput = ""
                        }) {
                            Text("Cancelar", color = ReadwoodTextMuted)
                        }
                    }
                    Button(
                        onClick = { guardarOActualizar() },
                        colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (idEnEdicion == null) "Agregar" else "Guardar Cambios")
                    }
                }
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ReadwoodPurple)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaItems, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReadwoodCardBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usuario: ${item.usuario}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ReadwoodTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Texto: ${item.texto}",
                                    fontSize = 14.sp,
                                    color = ReadwoodTextMuted
                                )
                            }

                            IconButton(onClick = {
                                idEnEdicion = item.id
                                usuarioInput = item.usuario
                                textoInput = item.texto
                            }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = ReadwoodPurple)
                            }

                            IconButton(onClick = { eliminarItem(item.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }
    }
}