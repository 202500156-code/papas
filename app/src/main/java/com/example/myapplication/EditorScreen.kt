package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var iniciarDirectoEnCapitulo by remember { mutableStateOf(false) }
    var mostrarSelectorNovelaCap by remember { mutableStateOf(false) }

    // Diálogo para elegir a cuál de tus novelas creadas añadirle otro capítulo
    if (mostrarSelectorNovelaCap) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorNovelaCap = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    "Añadir Capítulo a Novela",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.purple
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Elige la novela a la que deseas agregar un nuevo capítulo:",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    misNovelas.forEach { nov ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.bg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mostrarSelectorNovelaCap = false
                                    novelaEditando = nov
                                    iniciarDirectoEnCapitulo = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(nov.titulo, fontWeight = FontWeight.Bold, color = colors.textDark, fontSize = 14.sp)
                                Text(
                                    "${nov.capitulos.size} capítulo(s) • ${nov.categoria}",
                                    fontSize = 11.sp,
                                    color = colors.purple
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarSelectorNovelaCap = false }) {
                    Text("Cancelar", color = colors.textMuted)
                }
            }
        )
    }

    if (creandoNueva || novelaEditando != null) {
        FlujoDosVentanasNovela(
            usuario = usuario,
            novelaInicial = novelaEditando,
            abrirDirectoEnCapitulo = iniciarDirectoEnCapitulo,
            onCerrar = {
                creandoNueva = false
                novelaEditando = null
                iniciarDirectoEnCapitulo = false
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
                onClick = {
                    iniciarDirectoEnCapitulo = false
                    creandoNueva = true
                },
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

            // Botón para elegir una novela creada y añadirle otro capítulo directamente
            if (misNovelas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { mostrarSelectorNovelaCap = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.purple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agregar nuevo capítulo a novela existente", color = colors.purple, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
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
                                PortadaConTextoSuperpuesto(
                                    titulo = nov.titulo,
                                    autor = usuario.usuario,
                                    portadaBmp = portadaBmp,
                                    colorHex = nov.portadaColorHex,
                                    width = 56.dp,
                                    height = 82.dp,
                                    tituloSize = 9.sp,
                                    autorSize = 7.sp
                                )

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
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = "Abrir novela", tint = colors.purple)
                                }
                                IconButton(onClick = {
                                    iniciarDirectoEnCapitulo = false
                                    novelaEditando = nov
                                }) {
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
fun PortadaConTextoSuperpuesto(
    titulo: String,
    autor: String,
    portadaBmp: androidx.compose.ui.graphics.ImageBitmap?,
    colorHex: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    tituloSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    autorSize: androidx.compose.ui.unit.TextUnit = 10.sp
) {
    val colors = LocalReadwoodColors.current
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(parseHexColorSafe(colorHex, colors.purple))
    ) {
        if (portadaBmp != null) {
            Image(
                bitmap = portadaBmp,
                contentDescription = "Portada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Degradado superior oscuro para que el texto puesto arriba de la imagen siempre sea legible
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                        )
                    )
            )
        }

        // Texto superpuesto en la parte superior (Título + Nombre del autor)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo.ifBlank { "Sin título" },
                color = Color.White,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = tituloSize,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = autor,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = autorSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FlujoDosVentanasNovela(
    usuario: UsuarioPerfil,
    novelaInicial: Novela?,
    abrirDirectoEnCapitulo: Boolean = false,
    onCerrar: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var ventanaEscrituraCapitulo by remember { mutableStateOf(abrirDirectoEnCapitulo) }
    var capituloEditandoId by remember { mutableStateOf<String?>(null) }

    var titulo by remember { mutableStateOf(novelaInicial?.titulo ?: "") }
    var sinopsis by remember { mutableStateOf(novelaInicial?.sinopsis ?: "") }

    // Temáticas con slash (/) + opción de agregar otra
    val temasBase = listOf("Fantasía", "Romance", "Misterio", "Terror", "Ciencia Ficción", "Drama", "Aventura")
    var temasSeleccionados by remember {
        mutableStateOf(
            novelaInicial?.categoria?.split("/")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: listOf("Fantasía")
        )
    }
    var nuevoTemaInput by remember { mutableStateOf("") }

    // Etiquetas libres (de lo que sea, no afectan el buscador)
    var etiquetas by remember { mutableStateOf(novelaInicial?.etiquetas ?: emptyList()) }
    var nuevaEtiquetaInput by remember { mutableStateOf("") }

    // Portada: diseño o imagen exportada desde celular con texto encima
    var portadaBase64 by remember { mutableStateOf(novelaInicial?.portadaBase64 ?: "") }
    var portadaColorHex by remember { mutableStateOf(novelaInicial?.portadaColorHex ?: "#6B3A50") }
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

            // Editor Visual de Portada (con texto superpuesto arriba de la imagen y nombre del usuario fijo)
            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Editor de Portada", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    PortadaConTextoSuperpuesto(
                        titulo = titulo.ifBlank { "Tu Título Aquí" },
                        autor = usuario.usuario,
                        portadaBmp = portadaBmp,
                        colorHex = portadaColorHex,
                        width = 128.dp,
                        height = 186.dp,
                        tituloSize = 14.sp,
                        autorSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Color de fondo (si no subes imagen):", fontSize = 12.sp, color = colors.textMuted)
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

                    OutlinedButton(
                        onClick = { mostrarConsentimientoArchivo = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (portadaBase64.isNotEmpty()) "Cambiar imagen de portada" else "Subir imagen desde archivos del celular",
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
                value = sinopsis,
                onValueChange = { sinopsis = it },
                label = { Text("Sinopsis") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Temática con slash (/) + opción de agregar otra
            Text(
                text = "De qué trata (Temáticas con /): ${temasSeleccionados.joinToString(" / ")}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(temasBase) { tema ->
                    val activo = temasSeleccionados.any { it.equals(tema, ignoreCase = true) }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (activo) colors.purple else colors.cardBg,
                        border = if (activo) null else BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable {
                            temasSeleccionados = if (activo && temasSeleccionados.size > 1) {
                                temasSeleccionados.filterNot { it.equals(tema, ignoreCase = true) }
                            } else if (!activo) {
                                temasSeleccionados + tema
                            } else temasSeleccionados
                        }
                    ) {
                        Text(
                            text = tema,
                            fontSize = 12.sp,
                            color = if (activo) Color.White else colors.textDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevoTemaInput,
                    onValueChange = { nuevoTemaInput = it },
                    placeholder = { Text("Agregar otra temática...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    val limpio = ProfanityFilter.censurar(nuevoTemaInput.trim())
                    if (limpio.isNotEmpty() && temasSeleccionados.none { it.equals(limpio, true) }) {
                        temasSeleccionados = temasSeleccionados + limpio
                        nuevoTemaInput = ""
                    }
                }) {
                    Text("+ Añadir", color = colors.purple)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Etiquetas libres (de cualquier tema, no afectan el buscador principal)
            Text(
                text = "Etiquetas libres (Opcional)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevaEtiquetaInput,
                    onValueChange = { nuevaEtiquetaInput = it },
                    placeholder = { Text("Ej. enemigos a amantes, magia oscura...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    val tagLimpia = ProfanityFilter.censurar(nuevaEtiquetaInput.trim().removePrefix("#"))
                    if (tagLimpia.isNotEmpty() && etiquetas.none { it.equals(tagLimpia, true) }) {
                        etiquetas = etiquetas + tagLimpia
                        nuevaEtiquetaInput = ""
                    }
                }) {
                    Text("+ Etiqueta", color = colors.purple)
                }
            }
            if (etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(etiquetas) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.cardBg,
                            border = BorderStroke(1.dp, colors.purple)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", fontSize = 12.sp, color = colors.purple)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Quitar etiqueta",
                                    tint = colors.purple,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { etiquetas = etiquetas - tag }
                                )
                            }
                        }
                    }
                }
            }

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
            Text("Autor: ${usuario.usuario}", fontSize = 12.sp, color = colors.purple)
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
                                titulo = ProfanityFilter.censurar(capTitulo.trim()),
                                contenido = ProfanityFilter.censurar(capContenido.trim()),
                                esBorrador = !publicarAhora
                            )
                        } else existing
                    }
                } else {
                    capitulos.toList() + Capitulo(
                        id = UUID.randomUUID().toString(),
                        numero = capitulos.size + 1,
                        titulo = ProfanityFilter.censurar(capTitulo.trim()),
                        contenido = ProfanityFilter.censurar(capContenido.trim()),
                        esBorrador = !publicarAhora
                    )
                }

                val categoriaSlash = temasSeleccionados.joinToString(" / ").ifEmpty { "Fantasía" }

                val novelaFinal = Novela(
                    id = novelaInicial?.id ?: "",
                    autorId = usuario.id,
                    autor = usuario.usuario, // Siempre el nombre del usuario
                    titulo = ProfanityFilter.censurar(titulo.trim()),
                    sinopsis = ProfanityFilter.censurar(sinopsis.trim()),
                    categoria = categoriaSlash,
                    etiquetas = etiquetas,
                    portadaBase64 = portadaBase64,
                    portadaColorHex = portadaColorHex,
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