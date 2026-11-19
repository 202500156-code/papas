package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import java.util.UUID

@Composable
fun EditorScreen(
    usuario: UsuarioPerfil,
    misNovelas: List<Novela>,
    onAbrirNovela: (Novela) -> Unit
) {
    val colors = LocalReadwoodColors.current
    var novelaEditando by remember { mutableStateOf<Novela?>(null) }
    var creandoNueva by remember { mutableStateOf(false) }

    if (creandoNueva || novelaEditando != null) {
        FlujoDosVentanasNovela(
            usuario = usuario,
            novelaInicial = novelaEditando,
            onCerrar = {
                creandoNueva = false
                novelaEditando = null
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(16.dp)
        ) {
            Text(
                text = "Panel de Escritor",
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { creandoNueva = true },
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva Novela", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Mis Novelas y Borradores (Toca una para abrir)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (misNovelas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has escrito ninguna novela.", color = colors.textMuted)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(misNovelas, key = { it.id }) { nov ->
                        val portadaBmp = remember(nov.portadaBase64) { ImageUtils.base64ToImageBitmap(nov.portadaBase64) }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirNovela(nov) }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(52.dp)
                                        .height(74.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(parseHexColorSafe(nov.portadaColorHex, colors.purple)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (portadaBmp != null) {
                                        Image(bitmap = portadaBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Text(nov.titulo.take(8), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nov.titulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                    Text(
                                        text = if (nov.esPublicada) "Publicada • ${nov.capitulos.size} cap." else "🔒 Borrador privado (Solo tú la ves)",
                                        fontSize = 12.sp,
                                        color = colors.purple
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "♥ ${nov.meGusta} Likes  •  👁 ${nov.vistas} Vistas  •  💬 ${nov.totalComentarios} Comentarios",
                                        fontSize = 11.sp,
                                        color = colors.textMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { onAbrirNovela(nov) }) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = "Abrir y leer novela", tint = colors.purple)
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
private fun FlujoDosVentanasNovela(
    usuario: UsuarioPerfil,
    novelaInicial: Novela?,
    onCerrar: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var ventanaEscrituraCapitulo by remember { mutableStateOf(false) }
    var capituloEditandoId by remember { mutableStateOf<String?>(null) }

    var titulo by remember { mutableStateOf(novelaInicial?.titulo ?: "") }
    var sinopsis by remember { mutableStateOf(novelaInicial?.sinopsis ?: "") }
    var categoria by remember { mutableStateOf(novelaInicial?.categoria ?: "Fantasía") }

    var portadaBase64 by remember { mutableStateOf(novelaInicial?.portadaBase64 ?: "") }
    var portadaColorHex by remember { mutableStateOf(novelaInicial?.portadaColorHex ?: "#6B3A50") }
    var portadaSubtitulo by remember { mutableStateOf(novelaInicial?.portadaSubtitulo ?: "") }
    var mostrarConsentimientoArchivo by remember { mutableStateOf(false) }

    val archivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            portadaBase64 = ImageUtils.uriToCompressedBase64(context, uri, maxSide = 480)
        }
    }

    val capitulos = remember {
        mutableStateListOf<Capitulo>().apply {
            if (novelaInicial != null) addAll(novelaInicial.capitulos)
        }
    }

    var capTitulo by remember { mutableStateOf("") }
    var capContenido by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    if (mostrarConsentimientoArchivo) {
        AlertDialog(
            onDismissRequest = { mostrarConsentimientoArchivo = false },
            containerColor = colors.cardBg,
            title = { Text("Acceso a Archivos del Celular", fontWeight = FontWeight.Bold, color = colors.purple) },
            text = {
                Text(
                    "¿Das tu consentimiento para seleccionar una imagen desde los archivos de tu celular y usarla como portada de tu novela?",
                    color = colors.textDark,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConsentimientoArchivo = false
                        archivoLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) { Text("Aceptar y subir archivo", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConsentimientoArchivo = false }) { Text("Cancelar", color = colors.textMuted) }
            }
        )
    }

    if (!ventanaEscrituraCapitulo) {
        val portadaBmp = remember(portadaBase64) { ImageUtils.base64ToImageBitmap(portadaBase64) }
        val coloresDiseno = listOf("#6B3A50", "#2B4C65", "#3B5E47", "#7A442E", "#231F20")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCerrar) { Text("← Cancelar", color = colors.purple) }
                Text("Configurar Novela y Portada", fontWeight = FontWeight.Bold, color = colors.textDark)
            }

            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Editor de Portada", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(175.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(parseHexColorSafe(portadaColorHex, colors.purple)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (portadaBmp != null) {
                            Image(bitmap = portadaBmp, contentDescription = "Portada", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = titulo.ifBlank { "Tu Título Aquí" },
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = portadaSubtitulo.ifBlank { usuario.usuario },
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Diseñar fondo de portada:", fontSize = 12.sp, color = colors.textMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        coloresDiseno.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(hex, colors.purple))
                                    .border(if (portadaColorHex == hex && portadaBase64.isEmpty()) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable {
                                        portadaBase64 = ""
                                        portadaColorHex = hex
                                    }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = portadaSubtitulo,
                        onValueChange = { portadaSubtitulo = it },
                        label = { Text("Subtítulo o lema de portada") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { mostrarConsentimientoArchivo = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (portadaBase64.isNotEmpty()) "Cambiar portada desde archivos" else "Subir diseño desde archivos del celular",
                            color = colors.purple,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it; errorMsg = "" },
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

            if (capitulos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Capítulos existentes (Toca uno para editarlo):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                Spacer(modifier = Modifier.height(4.dp))
                capitulos.forEach { c ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                capituloEditandoId = c.id
                                capTitulo = c.titulo
                                capContenido = c.contenido
                                ventanaEscrituraCapitulo = true
                            }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cap. ${c.numero}: ${c.titulo}", fontSize = 13.sp, color = colors.textDark, fontWeight = FontWeight.SemiBold)
                            Text(if (c.esBorrador) "[BORRADOR]" else "[PUBLICADO]", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                }
            }

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        errorMsg = "Primero escribe el título de tu novela."
                    } else {
                        capituloEditandoId = null
                        capTitulo = ""
                        capContenido = ""
                        ventanaEscrituraCapitulo = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Escribir nuevo capítulo →", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { ventanaEscrituraCapitulo = false }) { Text("← Volver a Portada y Datos", color = colors.purple) }
                Text(
                    if (capituloEditandoId != null) "Editando Capítulo" else "Capítulo ${capitulos.size + 1}",
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted
                )
            }

            Text(titulo, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = capTitulo,
                onValueChange = { capTitulo = it; errorMsg = "" },
                label = { Text("Nombre del capítulo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = capContenido,
                onValueChange = { capContenido = it; errorMsg = "" },
                label = { Text("Escribe tu historia aquí...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp),
                minLines = 12
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            fun guardarConEstado(publicarAhora: Boolean) {
                if (capTitulo.isBlank() || capContenido.isBlank()) {
                    errorMsg = "Escribe el nombre y el contenido del capítulo antes de continuar."
                    return
                }
                guardando = true

                val listaFinalCaps = if (capituloEditandoId != null) {
                    capitulos.map { existing ->
                        if (existing.id == capituloEditandoId) {
                            existing.copy(
                                titulo = capTitulo.trim(),
                                contenido = capContenido.trim(),
                                esBorrador = !publicarAhora
                            )
                        } else existing
                    }
                } else {
                    capitulos.toList() + Capitulo(
                        id = UUID.randomUUID().toString(),
                        numero = capitulos.size + 1,
                        titulo = capTitulo.trim(),
                        contenido = capContenido.trim(),
                        esBorrador = !publicarAhora
                    )
                }

                val novelaFinal = Novela(
                    id = novelaInicial?.id ?: "",
                    autorId = usuario.id,
                    autor = usuario.usuario,
                    titulo = titulo.trim(),
                    sinopsis = sinopsis.trim(),
                    categoria = categoria.trim().ifEmpty { "Fantasía" },
                    portadaBase64 = portadaBase64,
                    portadaColorHex = portadaColorHex,
                    portadaSubtitulo = portadaSubtitulo.trim(),
                    esPublicada = publicarAhora || listaFinalCaps.any { !it.esBorrador },
                    meGusta = novelaInicial?.meGusta ?: 0,
                    noMeGusta = novelaInicial?.noMeGusta ?: 0,
                    vistas = novelaInicial?.vistas ?: 0,
                    totalComentarios = novelaInicial?.totalComentarios ?: 0,
                    capitulos = listaFinalCaps
                )
                FirestoreService.guardarNovela(
                    novela = novelaFinal,
                    onSuccess = { guardando = false; onCerrar() },
                    onFailure = { guardando = false; errorMsg = "Error al guardar en Firebase" }
                )
            }

            OutlinedButton(
                onClick = { guardarConEstado(publicarAhora = false) },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Guardar como Borrador (Solo yo lo veo)", color = colors.purple, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { guardarConEstado(publicarAhora = true) },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Publicar Novela", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}