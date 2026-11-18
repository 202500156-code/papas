package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import com.example.myapplication.ui.theme.LocalReadwoodColors
import java.util.UUID

@Composable
fun EditorScreen(
    usuario: UsuarioPerfil,
    misNovelas: List<Novela>
) {
    val colors = LocalReadwoodColors.current
    var novelaEditando by remember { mutableStateOf<Novela?>(null) }
    var creandoNueva by remember { mutableStateOf(false) }

    if (creandoNueva || novelaEditando != null) {
        FormularioNovelaYCapitulos(
            usuario = usuario,
            novelaInicial = novelaEditando,
            onCerrar = {
                creandoNueva = false
                novelaEditando = null
            }
        )
    } else {
        // Panel de Escritor
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Panel de Escritor", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                Button(
                    onClick = { creandoNueva = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                    Text(" Nueva Novela", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalVistas = misNovelas.sumOf { it.vistas }
            val totalLikes = misNovelas.sumOf { it.meGusta }
            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${misNovelas.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.purple)
                        Text("Obras", fontSize = 12.sp, color = colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalVistas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.purple)
                        Text("Lecturas", fontSize = 12.sp, color = colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalLikes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.purple)
                        Text("Likes", fontSize = 12.sp, color = colors.textMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Mis Novelas y Borradores", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
            Spacer(modifier = Modifier.height(8.dp))

            if (misNovelas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has escrito ninguna novela.", color = colors.textMuted)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(misNovelas, key = { it.id }) { nov ->
                        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nov.titulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                    Text(
                                        text = if (nov.esPublicada) "Publicada • ${nov.capitulos.size} cap." else "🔒 Borrador privado (Solo tú la ves)",
                                        fontSize = 12.sp,
                                        color = colors.purple
                                    )
                                }
                                IconButton(onClick = { novelaEditando = nov }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = colors.purple)
                                }
                                IconButton(onClick = { FirestoreService.eliminarNovela(nov.id) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioNovelaYCapitulos(
    usuario: UsuarioPerfil,
    novelaInicial: Novela?,
    onCerrar: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    var titulo by remember { mutableStateOf(novelaInicial?.titulo ?: "") }
    var sinopsis by remember { mutableStateOf(novelaInicial?.sinopsis ?: "") }
    var categoria by remember { mutableStateOf(novelaInicial?.categoria ?: "Fantasía") }
    var esPublicada by remember { mutableStateOf(novelaInicial?.esPublicada ?: false) } // Por defecto inicia como borrador privado

    val capitulos = remember {
        mutableStateListOf<Capitulo>().apply {
            if (novelaInicial != null) addAll(novelaInicial.capitulos)
        }
    }

    var capTitulo by remember { mutableStateOf("") }
    var capContenido by remember { mutableStateOf("") }
    var capEsBorrador by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCerrar) { Text("← Cancelar", color = colors.purple) }
            Text(if (novelaInicial == null) "Nueva Novela" else "Editar Novela", fontWeight = FontWeight.Bold, color = colors.textDark)
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título de la novela") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Género (Fantasía, Romance, Misterio...)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sinopsis,
            onValueChange = { sinopsis = it },
            label = { Text("Sinopsis") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Switch(checked = esPublicada, onCheckedChange = { esPublicada = it })
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (esPublicada) "Pública (Visible para todos los lectores)" else "Borrador privado (Solo visible para ti)",
                fontSize = 13.sp,
                color = colors.purple,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        Text("Agregar / Editar Capítulo", fontWeight = FontWeight.Bold, color = colors.textDark)

        OutlinedTextField(
            value = capTitulo,
            onValueChange = { capTitulo = it },
            label = { Text("Título del capítulo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = capContenido,
            onValueChange = { capContenido = it },
            label = { Text("Contenido del capítulo (${capContenido.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} palabras)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = capEsBorrador, onCheckedChange = { capEsBorrador = it })
            Text("Guardar este capítulo como borrador (solo tú lo ves)", fontSize = 12.sp, color = colors.textMuted)
        }

        OutlinedButton(
            onClick = {
                if (capTitulo.isNotBlank() && capContenido.isNotBlank()) {
                    capitulos.add(
                        Capitulo(
                            id = UUID.randomUUID().toString(),
                            numero = capitulos.size + 1,
                            titulo = capTitulo.trim(),
                            contenido = capContenido.trim(),
                            esBorrador = capEsBorrador
                        )
                    )
                    capTitulo = ""
                    capContenido = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Añadir capítulo a la lista", color = colors.purple)
        }

        Spacer(modifier = Modifier.height(8.dp))
        capitulos.forEach { c ->
            Text("• Cap. ${c.numero}: ${c.titulo} ${if (c.esBorrador) "[BORRADOR]" else "[PUBLICADO]"}", fontSize = 12.sp, color = colors.textDark)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (titulo.isBlank()) return@Button
                guardando = true
                val novelaFinal = Novela(
                    id = novelaInicial?.id ?: "",
                    autorId = usuario.id,
                    autor = usuario.usuario,
                    titulo = titulo.trim(),
                    sinopsis = sinopsis.trim(),
                    categoria = categoria.trim().ifEmpty { "Fantasía" },
                    esPublicada = esPublicada,
                    meGusta = novelaInicial?.meGusta ?: 0,
                    noMeGusta = novelaInicial?.noMeGusta ?: 0,
                    vistas = novelaInicial?.vistas ?: 0,
                    capitulos = capitulos.toList()
                )
                FirestoreService.guardarNovela(
                    novela = novelaFinal,
                    onSuccess = { guardando = false; onCerrar() },
                    onFailure = { guardando = false }
                )
            },
            enabled = !guardando,
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (esPublicada) "Guardar y Publicar Novela" else "Guardar Borrador Privado", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}