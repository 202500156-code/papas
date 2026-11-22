package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EscribirScreen(
    usuario: UsuarioPerfil,
    misNovelas: List<Novela>,
    novelaAEditar: Novela? = null,
    onLimpiarEdicion: () -> Unit = {}
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var novelaAbiertaId by remember(novelaAEditar) { mutableStateOf(novelaAEditar?.id) }
    var creandoNuevaNovela by remember { mutableStateOf(false) }
    var editandoCapituloIndice by remember { mutableStateOf<Int?>(null) }

    val novelaAbierta = misNovelas.find { it.id == novelaAbiertaId }

    // VISTA 3: EDITOR DIRECTO DE CAPÍTULO (Aquí se escribe y se elige Publicar o Borrador)
    if (novelaAbierta != null && editandoCapituloIndice != null) {
        val esNuevoCap = editandoCapituloIndice == -1
        val capExistente = if (!esNuevoCap) novelaAbierta.capitulos.getOrNull(editandoCapituloIndice!!) else null
        val numeroCap = if (esNuevoCap) novelaAbierta.capitulos.size + 1 else (capExistente?.numero ?: 1)

        var tituloCapitulo by remember(novelaAbierta.id, editandoCapituloIndice) {
            mutableStateOf(capExistente?.titulo ?: "")
        }
        var contenidoCapitulo by remember(novelaAbierta.id, editandoCapituloIndice) {
            mutableStateOf(capExistente?.contenido ?: "")
        }
        var capituloComoBorrador by remember(novelaAbierta.id, editandoCapituloIndice) {
            mutableStateOf(capExistente?.esBorrador ?: false)
        }
        var mensajeCap by remember { mutableStateOf("") }
        var mostrarDialogoPubCap by remember { mutableStateOf(false) }
        var anuncioComunidadCap by remember { mutableStateOf("") }

        BackHandler {
            editandoCapituloIndice = null
        }

        fun guardarCapituloEnNovela(publicarNovela: Boolean, fechaProgramada: Long) {
            if (contenidoCapitulo.isBlank()) {
                mensajeCap = "Escribe el contenido del capítulo."
                return
            }

            val capFinal = Capitulo(
                id = capExistente?.id ?: UUID.randomUUID().toString(),
                numero = numeroCap,
                titulo = tituloCapitulo.trim().ifEmpty { "Capítulo $numeroCap" },
                contenido = contenidoCapitulo.trim(),
                esBorrador = capituloComoBorrador,
                corazones = capExistente?.corazones ?: 0,
                meGusta = capExistente?.meGusta ?: 0,
                noMeGusta = capExistente?.noMeGusta ?: 0,
                vistas = capExistente?.vistas ?: 0,
                comentariosCount = capExistente?.comentariosCount ?: 0,
                usuariosCorazon = capExistente?.usuariosCorazon ?: emptyList(),
                usuariosLike = capExistente?.usuariosLike ?: emptyList(),
                usuariosDislike = capExistente?.usuariosDislike ?: emptyList(),
                usuariosVistos = capExistente?.usuariosVistos ?: emptyList()
            )

            val nuevaListaCaps = if (esNuevoCap) {
                novelaAbierta.capitulos + capFinal
            } else {
                novelaAbierta.capitulos.mapIndexed { idx, c ->
                    if (idx == editandoCapituloIndice) capFinal else c
                }
            }

            val novelaActualizada = novelaAbierta.copy(
                capitulos = nuevaListaCaps,
                esPublicada = if (publicarNovela) true else novelaAbierta.esPublicada,
                fechaPublicacionProgramada = if (!publicarNovela && fechaProgramada > 0L) fechaProgramada else novelaAbierta.fechaPublicacionProgramada
            )

            FirestoreService.guardarNovela(
                novela = novelaActualizada,
                onSuccess = { novelaId ->
                    if (!publicarNovela && fechaProgramada > System.currentTimeMillis()) {
                        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(fechaProgramada))
                        val textoAnuncio = anuncioComunidadCap.ifBlank {
                            "¡Nuevo capítulo programado para '${novelaActualizada.titulo}' el $formatoFecha!"
                        }
                        FirestoreService.publicarEnComunidad(
                            autor = usuario,
                            mensaje = textoAnuncio,
                            novelaProgramadaId = novelaId,
                            novelaProgramadaTitulo = novelaActualizada.titulo,
                            fechaLanzamiento = fechaProgramada
                        )
                    }
                    editandoCapituloIndice = null
                },
                onFailure = { mensajeCap = "Error al guardar: ${it.message}" }
            )
        }

        if (mostrarDialogoPubCap) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoPubCap = false },
                containerColor = colors.cardBg,
                title = { Text("Opciones de Publicación", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.textDark) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("¿Cómo deseas publicar el Capítulo $numeroCap?", fontSize = 13.sp, color = colors.textDark)

                        Button(
                            onClick = {
                                capituloComoBorrador = false
                                mostrarDialogoPubCap = false
                                guardarCapituloEnNovela(publicarNovela = true, fechaProgramada = 0L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚀 Publicar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = colors.border)

                        Text("O programar lanzamiento en Comunidad:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.purple)
                        OutlinedTextField(
                            value = anuncioComunidadCap,
                            onValueChange = { anuncioComunidadCap = it },
                            placeholder = { Text("Mensaje opcional para tus lectores...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val elegida = Calendar.getInstance().apply {
                                                    set(year, month, day, hour, minute, 0)
                                                }.timeInMillis
                                                if (elegida > System.currentTimeMillis()) {
                                                    capituloComoBorrador = false
                                                    mostrarDialogoPubCap = false
                                                    guardarCapituloEnNovela(publicarNovela = false, fechaProgramada = elegida)
                                                } else {
                                                    mensajeCap = "Elige una fecha y hora futura."
                                                }
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            border = BorderStroke(1.dp, colors.purple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⏳ Programar fecha y hora", color = colors.purple, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoPubCap = false }) {
                        Text("Cancelar", color = colors.purple)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                TextButton(onClick = { editandoCapituloIndice = null }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("← Volver a ${novelaAbierta.titulo}", color = colors.purple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Text(
                text = if (esNuevoCap) "Nuevo Capítulo ($numeroCap)" else "Editando Capítulo $numeroCap",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )

            OutlinedTextField(
                value = tituloCapitulo,
                onValueChange = { tituloCapitulo = it },
                label = { Text("Título del Capítulo $numeroCap") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contenidoCapitulo,
                onValueChange = { contenidoCapitulo = it },
                label = { Text("Contenido del capítulo...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 12
            )

            if (mensajeCap.isNotEmpty()) {
                Text(mensajeCap, color = colors.purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        capituloComoBorrador = true
                        guardarCapituloEnNovela(publicarNovela = false, fechaProgramada = 0L)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Borrador", color = colors.purple, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        if (contenidoCapitulo.isBlank()) {
                            mensajeCap = "Escribe el contenido antes de publicar."
                        } else {
                            capituloComoBorrador = false
                            mostrarDialogoPubCap = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Publicar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
        return
    }

    // VISTA 2: DETALLE DE NOVELA EN EL PANEL DE ESCRITOR
    if (novelaAbierta != null) {
        BackHandler {
            novelaAbiertaId = null
            onLimpiarEdicion()
        }

        val portadaBmp = remember(novelaAbierta.portadaBase64) {
            ImageUtils.base64ToImageBitmap(novelaAbierta.portadaBase64)
        }
        var estaTerminadaLocal by remember(novelaAbierta.id, novelaAbierta.estaTerminada) {
            mutableStateOf(novelaAbierta.estaTerminada)
        }
        var mensajeEstadoObra by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        novelaAbiertaId = null
                        onLimpiarEdicion()
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                PortadaConTextoSuperpuesto(
                    titulo = novelaAbierta.titulo,
                    autor = novelaAbierta.autor,
                    portadaBmp = portadaBmp,
                    colorHex = novelaAbierta.portadaColorHex,
                    width = 96.dp,
                    height = 138.dp,
                    tituloSize = 12.sp,
                    autorSize = 9.sp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(novelaAbierta.categoria.uppercase(), fontSize = 11.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                    Text(novelaAbierta.titulo, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                    Text("Por ${novelaAbierta.autor}", fontSize = 13.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "♥ ${novelaAbierta.corazones}  •  👍 ${novelaAbierta.meGusta}  •  👎 ${novelaAbierta.noMeGusta}\n👁 ${novelaAbierta.vistas} Vistas  •  💬 ${novelaAbierta.totalComentarios} Comentarios",
                        fontSize = 11.sp,
                        color = colors.purple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(novelaAbierta.sinopsis, fontSize = 14.sp, color = colors.textDark)

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = colors.cardBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (estaTerminadaLocal) "✓ Obra Terminada" else "⏳ Obra en Emisión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (estaTerminadaLocal) Color(0xFF2E7D32) else colors.textDark
                            )
                            Text(
                                text = "Indica a tus lectores si la historia ya concluyó",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = estaTerminadaLocal,
                            onCheckedChange = { terminada ->
                                estaTerminadaLocal = terminada
                                FirestoreService.guardarNovela(
                                    novela = novelaAbierta.copy(estaTerminada = terminada),
                                    onSuccess = { mensajeEstadoObra = "" },
                                    onFailure = {
                                        estaTerminadaLocal = !terminada
                                        mensajeEstadoObra = "Error al guardar estado: ${it.message}"
                                    }
                                )
                            }
                        )
                    }
                    if (mensajeEstadoObra.isNotEmpty()) {
                        Text(
                            text = mensajeEstadoObra,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (novelaAbierta.etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(novelaAbierta.etiquetas) { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.cardBg,
                            border = BorderStroke(0.8.dp, colors.border)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                color = colors.purple,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Capítulos (${novelaAbierta.capitulos.size})",
                fontFamily = FontFamily.Serif,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            novelaAbierta.capitulos.forEachIndexed { index, cap ->
                Surface(
                    color = colors.cardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { editandoCapituloIndice = index }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Capítulo ${cap.numero}: ${cap.titulo}",
                            fontWeight = FontWeight.Bold,
                            color = colors.textDark
                        )
                        if (cap.esBorrador) {
                            Text("BORRADOR PRIVADO", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.purple.copy(alpha = 0.14f),
                border = BorderStroke(1.2.dp, colors.purple),
                modifier = Modifier
                    .size(52.dp)
                    .clickable { editandoCapituloIndice = -1 }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Agregar capítulo",
                        tint = colors.purple,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
        return
    }

    // VISTA 4: CREAR NUEVA NOVELA (Datos, Género con +, Etiquetas con +, y botón Escribir)
    // VISTA 4: CREAR NUEVA NOVELA
    if (creandoNuevaNovela) {
        BackHandler { creandoNuevaNovela = false }

        var tituloNovela by remember { mutableStateOf("") }
        var sinopsis by remember { mutableStateOf("") }
        var generosSeleccionados by remember { mutableStateOf(emptyList<String>()) }
        var generoExtraInput by remember { mutableStateOf("") }
        var mostrarCampoGeneroExtra by remember { mutableStateOf(false) }

        var nuevaEtiquetaInput by remember { mutableStateOf("") }
        var listaEtiquetas by remember { mutableStateOf(listOf<String>()) }

        var portadaBase64 by remember { mutableStateOf("") }
        var colorPortadaHex by remember { mutableStateOf("#5E2D40") }
        var mensajeEstado by remember { mutableStateOf("") }
        var uriPortadaPendiente by remember { mutableStateOf<Uri?>(null) }

        val selectorPortada = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) uriPortadaPendiente = uri
        }

        if (uriPortadaPendiente != null) {
            AlertDialog(
                onDismissRequest = { uriPortadaPendiente = null },
                title = { Text("Permiso de Imagen de Portada") },
                text = { Text("¿Confirmas que deseas usar esta imagen de tu galería como portada de tu novela?") },
                confirmButton = {
                    Button(onClick = {
                        val b64 = ImageUtils.uriToCompressedBase64(context, uriPortadaPendiente!!)
                        if (b64.isNotEmpty()) {
                            portadaBase64 = b64
                            mensajeEstado = "Portada cargada correctamente."
                        }
                        uriPortadaPendiente = null
                    }) { Text("Aceptar") }
                },
                dismissButton = {
                    TextButton(onClick = { uriPortadaPendiente = null }) { Text("Cancelar") }
                }
            )
        }

        val generos = listOf(
            "Fantasía",
            "Romance",
            "Misterio",
            "Suspenso / Thriller",
            "Terror / Horror",
            "Ciencia Ficción",
            "Aventura",
            "Drama",
            "Juvenil (YA)",
            "New Adult",
            "Ficción Histórica",
            "Paranormal",
            "Fanfiction",
            "Humor / Comedia",
            "Ficción Contemporánea",
            "Policiaco / Noir",
            "Mitología y Folclore",
            "Steampunk",
            "Western",
            "Realismo Mágico",
            "Poesía",
            "Relato Corto",
            "Crónica / Ensayo",
            "Biografía / Memorias",
            "Teatro / Guion"
        )
        val generosDisponibles = generos.filter { gen ->
            generosSeleccionados.none { it.equals(gen, ignoreCase = true) }
        }
        val coloresPortada = listOf("#5E2D40", "#1E3A5F", "#2E4A3E", "#4A2C5E", "#7A3B2E", "#2C2C2C")
        val portadaPreviewBmp = remember(portadaBase64) { ImageUtils.base64ToImageBitmap(portadaBase64) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                TextButton(onClick = { creandoNuevaNovela = false }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)) {
                    Text("← Volver al Panel", color = colors.purple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Text("Nueva Novela", fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
            Spacer(modifier = Modifier.height(10.dp))

            // PORTADA
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PortadaConTextoSuperpuesto(
                    titulo = tituloNovela.ifBlank { "Tu Título" },
                    autor = usuario.usuario,
                    portadaBmp = portadaPreviewBmp,
                    colorHex = colorPortadaHex,
                    width = 88.dp,
                    height = 128.dp,
                    tituloSize = 11.sp,
                    autorSize = 9.sp
                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectorPortada.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (portadaBase64.isEmpty()) "Subir imagen de portada" else "Cambiar imagen", fontSize = 12.sp, color = colors.purple)
                    }

                    if (portadaBase64.isNotEmpty()) {
                        TextButton(onClick = { portadaBase64 = "" }) {
                            Text("Quitar imagen (usar color)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        coloresPortada.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(hex, colors.purple))
                                    .border(2.dp, if (colorPortadaHex == hex) colors.textDark else Color.Transparent, CircleShape)
                                    .clickable {
                                        portadaBase64 = ""
                                        colorPortadaHex = hex
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. TÍTULO Y SINOPSIS ARRIBA
            OutlinedTextField(
                value = tituloNovela,
                onValueChange = { tituloNovela = it },
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

            // 2. RECTÁNGULO DE GÉNEROS SELECCIONADOS + LISTA DE GÉNEROS + BOTÓN "+"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selecciona Géneros",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textDark
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (mostrarCampoGeneroExtra) colors.purple else colors.cardBg,
                    border = BorderStroke(1.dp, colors.purple),
                    modifier = Modifier.clickable {
                        mostrarCampoGeneroExtra = !mostrarCampoGeneroExtra
                        if (!mostrarCampoGeneroExtra) generoExtraInput = ""
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (mostrarCampoGeneroExtra) Icons.Outlined.Close else Icons.Outlined.Add,
                            contentDescription = if (mostrarCampoGeneroExtra) "Cerrar género extra" else "Agregar otro género",
                            tint = if (mostrarCampoGeneroExtra) Color.White else colors.purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (mostrarCampoGeneroExtra) "Cerrar" else "Otro género",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mostrarCampoGeneroExtra) Color.White else colors.purple
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Rectángulo superior con los géneros seleccionados
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.cardBg,
                border = BorderStroke(1.2.dp, colors.purple),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                if (generosSeleccionados.isEmpty()) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Toca un género de la lista o agrega uno con +",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(generosSeleccionados) { genSel ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.purple.copy(alpha = 0.18f),
                                border = BorderStroke(0.8.dp, colors.purple),
                                modifier = Modifier.clickable {
                                    generosSeleccionados = generosSeleccionados.filter { it != genSel }
                                }
                            ) {
                                Text(
                                    text = "$genSel  ✕",
                                    fontSize = 12.sp,
                                    color = colors.purple,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (mostrarCampoGeneroExtra) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = generoExtraInput,
                        onValueChange = { generoExtraInput = it },
                        label = { Text("Escribe otro género") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.purple,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                val limpio = generoExtraInput.trim()
                                if (limpio.isNotEmpty() && generosSeleccionados.none { it.equals(limpio, ignoreCase = true) }) {
                                    generosSeleccionados = generosSeleccionados + limpio
                                }
                                generoExtraInput = ""
                                mostrarCampoGeneroExtra = false
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Confirmar género extra",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.cardBg,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                generoExtraInput = ""
                                mostrarCampoGeneroExtra = false
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cancelar género extra",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de todos los géneros disponibles (al tocar uno sube al rectángulo y desaparece de aquí)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.cardBg,
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 175.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(generosDisponibles) { gen ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    generosSeleccionados = generosSeleccionados + gen
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = gen,
                                fontSize = 13.sp,
                                color = colors.textDark
                            )
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Agregar $gen",
                                tint = colors.purple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. ETIQUETAS CON RECTÁNGULO Y BOTÓN "+"
            Text(
                text = "Etiquetas",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nuevaEtiquetaInput,
                    onValueChange = { nuevaEtiquetaInput = it },
                    label = { Text("Añadir etiquetas") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.purple,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            val limpia = nuevaEtiquetaInput.trim().removePrefix("#")
                            if (limpia.isNotEmpty() && listaEtiquetas.none { it.equals(limpia, ignoreCase = true) }) {
                                listaEtiquetas = listaEtiquetas + limpia
                            }
                            nuevaEtiquetaInput = ""
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Añadir etiqueta",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            if (listaEtiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.cardBg,
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(listaEtiquetas) { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.purple.copy(alpha = 0.14f),
                                border = BorderStroke(0.8.dp, colors.purple),
                                modifier = Modifier.clickable {
                                    listaEtiquetas = listaEtiquetas.filter { it != tag }
                                }
                            ) {
                                Text(
                                    text = "#$tag  ✕",
                                    fontSize = 12.sp,
                                    color = colors.purple,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (mensajeEstado.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(mensajeEstado, color = colors.purple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. BOTÓN ESCRIBIR (Manda directo al editor del Capítulo 1)
            Button(
                onClick = {
                    if (tituloNovela.isBlank()) {
                        mensajeEstado = "Escribe primero el título de la novela."
                        return@Button
                    }

                    val tagPendiente = nuevaEtiquetaInput.trim().removePrefix("#")
                    val etiquetasFinales = if (tagPendiente.isNotEmpty() && listaEtiquetas.none { it.equals(tagPendiente, ignoreCase = true) }) {
                        listaEtiquetas + tagPendiente
                    } else {
                        listaEtiquetas
                    }

                    val genExtraPendiente = generoExtraInput.trim()
                    val listaGenerosFinal = if (genExtraPendiente.isNotEmpty() && generosSeleccionados.none { it.equals(genExtraPendiente, ignoreCase = true) }) {
                        generosSeleccionados + genExtraPendiente
                    } else {
                        generosSeleccionados
                    }

                    val categoriaFinal = if (listaGenerosFinal.isEmpty()) {
                        "General"
                    } else {
                        listaGenerosFinal.joinToString(" • ")
                    }

                    val nueva = Novela(
                        autorId = usuario.id,
                        autor = usuario.usuario,
                        titulo = tituloNovela.trim(),
                        sinopsis = sinopsis.trim().ifEmpty { "Sinopsis pendiente." },
                        categoria = categoriaFinal,
                        etiquetas = etiquetasFinales,
                        portadaBase64 = portadaBase64,
                        portadaColorHex = colorPortadaHex,
                        esPublicada = false,
                        capitulos = emptyList()
                    )

                    FirestoreService.guardarNovela(
                        novela = nueva,
                        onSuccess = { novelaGuardadaId ->
                            creandoNuevaNovela = false
                            novelaAbiertaId = novelaGuardadaId
                            editandoCapituloIndice = -1
                        },
                        onFailure = { mensajeEstado = "Error al crear novela: ${it.message}" }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escribir", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
        return
    }

    // VISTA 1: PANEL DE ESCRITOR
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Panel de Escritor",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { creandoNuevaNovela = true },
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nueva Novela", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Mis Novelas y Borradores (Toca una para abrir)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (misNovelas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes novelas creadas.", color = colors.textMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                items(misNovelas, key = { it.id }) { nov ->
                    val portadaBmp = remember(nov.portadaBase64) {
                        ImageUtils.base64ToImageBitmap(nov.portadaBase64)
                    }
                    val estadoTexto = when {
                        nov.esPublicada -> "Publicada"
                        nov.fechaPublicacionProgramada > System.currentTimeMillis() -> "Programada"
                        else -> "Borrador"
                    }

                    Surface(
                        color = colors.cardBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { novelaAbiertaId = nov.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PortadaConTextoSuperpuesto(
                                titulo = nov.titulo,
                                autor = nov.autor,
                                portadaBmp = portadaBmp,
                                colorHex = nov.portadaColorHex,
                                width = 56.dp,
                                height = 82.dp,
                                tituloSize = 9.sp,
                                autorSize = 7.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nov.titulo,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = colors.textDark
                                )
                                Text(
                                    text = "$estadoTexto • ${nov.capitulos.size} cap.",
                                    fontSize = 12.sp,
                                    color = colors.purple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "♥ ${nov.meGusta} Likes • 👁 ${nov.vistas} Vistas • 💬 ${nov.totalComentarios} Comentarios",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }

                            IconButton(
                                onClick = {
                                    FirestoreService.eliminarNovela(nov.id) {}
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Eliminar novela",
                                    tint = colors.textDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}