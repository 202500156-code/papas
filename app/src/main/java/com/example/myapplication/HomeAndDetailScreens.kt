package com.example.myapplication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

@Composable
fun IconoBocinaReporte(
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f)

        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.38f)
            lineTo(w * 0.42f, h * 0.38f)
            lineTo(w * 0.75f, h * 0.18f)
            lineTo(w * 0.75f, h * 0.82f)
            lineTo(w * 0.42f, h * 0.62f)
            lineTo(w * 0.18f, h * 0.62f)
            close()
        }
        drawPath(path = path, color = tint, style = stroke)

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.25f, h * 0.62f),
            size = Size(w * 0.12f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.70f, h * 0.32f),
            size = Size(w * 0.22f, h * 0.36f),
            style = stroke
        )
    }
}

@Composable
fun IconoOjoVisibilidad(
    oculto: Boolean,
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.09f)

        val ojoPath = Path().apply {
            moveTo(w * 0.10f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.16f, w * 0.90f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.84f, w * 0.10f, h * 0.50f)
            close()
        }
        drawPath(path = ojoPath, color = tint, style = stroke)

        drawCircle(
            color = tint,
            radius = w * 0.13f,
            center = Offset(w * 0.50f, h * 0.50f)
        )

        if (oculto) {
            drawLine(
                color = tint,
                start = Offset(w * 0.16f, h * 0.84f),
                end = Offset(w * 0.84f, h * 0.16f),
                strokeWidth = w * 0.10f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: UsuarioPerfil,
    novelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    usuariosQueSigo: List<UsuarioSeguimiento>,
    notificacionesSinLeer: Int,
    onAbrirNotificaciones: () -> Unit,
    onSeleccionarNovela: (Novela) -> Unit,
    onAbrirPerfilAutor: (String) -> Unit
) {
    val colors = LocalReadwoodColors.current
    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Todos") }
    val generos = listOf("Todos", "Fantasía", "Romance", "Misterio", "Terror", "Ciencia Ficción")

    var resultadosUsuarios by remember { mutableStateOf(emptyList<UsuarioPerfil>()) }

    LaunchedEffect(textoBusqueda) {
        if (textoBusqueda.trim().isNotEmpty()) {
            FirestoreService.buscarUsuarios(textoBusqueda) { lista ->
                resultadosUsuarios = lista.filter { it.id != usuario.id }
            }
        } else {
            resultadosUsuarios = emptyList()
        }
    }

    val novelasPublicadas = remember(novelas) {
        novelas.filter { it.esPublicada && it.id.isNotBlank() && it.titulo.isNotBlank() }
    }
    val q = textoBusqueda.trim()

    val filtradas = remember(novelasPublicadas, q, generoSeleccionado) {
        novelasPublicadas.filter { n ->
            val coincideTexto = q.isEmpty() ||
                    n.titulo.contains(q, ignoreCase = true) ||
                    n.autor.contains(q, ignoreCase = true) ||
                    n.etiquetas.any { it.contains(q, ignoreCase = true) }
            val coincideGenero = generoSeleccionado == "Todos" || n.categoria.contains(generoSeleccionado, ignoreCase = true)
            coincideTexto && coincideGenero
        }
    }

    val novelasSiguiendoLeyendo = remember(seguimientos, novelasPublicadas) {
        seguimientos.mapNotNull { rel ->
            val nov = novelasPublicadas.find { it.id == rel.novelaId }
            if (nov != null) rel to nov else null
        }
    }

    val novelasRecomendadas = remember(novelasPublicadas, seguimientos, usuariosQueSigo) {
        val idsLeidas = seguimientos.map { it.novelaId }.toSet()
        val novelasBase = novelasPublicadas.filter { it.id in idsLeidas }
        val autoresAfin = (novelasBase.map { it.autorId } + usuariosQueSigo.map { it.seguidoId }).toSet()
        val generosAfin = novelasBase.map { it.categoria.lowercase() }.toSet()
        val etiquetasAfin = novelasBase.flatMap { n -> n.etiquetas.map { it.lowercase() } }.toSet()

        if (autoresAfin.isEmpty() && generosAfin.isEmpty() && etiquetasAfin.isEmpty()) {
            emptyList()
        } else {
            novelasPublicadas
                .filter { it.id !in idsLeidas && it.autorId != usuario.id }
                .map { cand ->
                    var puntaje = 0
                    if (cand.autorId in autoresAfin) puntaje += 5
                    if (cand.categoria.lowercase() in generosAfin) puntaje += 3
                    val tagsComunes = cand.etiquetas.count { it.lowercase() in etiquetasAfin }
                    puntaje += tagsComunes * 2
                    cand to puntaje
                }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .map { it.first }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Column {
                        Text(
                            text = "EDICIÓN DE ${usuario.usuario.uppercase()}",
                            fontSize = 10.sp,
                            color = colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Readwood",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = colors.purple,
                            fontSize = 23.sp
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (notificacionesSinLeer > 0) {
                                Badge { Text("$notificacionesSinLeer") }
                            }
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        IconButton(onClick = onAbrirNotificaciones) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = colors.purple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bg)
            )
        },
        containerColor = colors.bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = "Buscar novelas, autores o lectores...",
                        color = colors.textMuted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                textStyle = TextStyle(fontSize = 14.sp, color = colors.textDark),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Limpiar", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(generos) { gen ->
                    val sel = generoSeleccionado == gen
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (sel) colors.purple else colors.cardBg,
                        border = BorderStroke(0.8.dp, if (sel) colors.purple else colors.border),
                        modifier = Modifier.clickable { generoSeleccionado = gen }
                    ) {
                        Text(
                            text = gen,
                            fontSize = 12.sp,
                            color = if (sel) Color.White else colors.textDark,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (q.isNotEmpty() && resultadosUsuarios.isEmpty() && filtradas.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron autores, lectores ni novelas para \"$q\".", color = colors.textMuted, fontSize = 13.sp)
                }
            } else if (q.isEmpty() && filtradas.isEmpty() && novelasSiguiendoLeyendo.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = colors.cardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Aún no hay novelas publicadas en este género. ¡Sé el primero en crear una desde la pestaña Escribir!",
                        color = colors.textMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    if (q.isEmpty() && novelasSiguiendoLeyendo.isNotEmpty()) {
                        item(key = "seccion_seguir_leyendo") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Seguir leyendo",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textDark
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(novelasSiguiendoLeyendo, key = { it.first.id }) { (rel, nov) ->
                                        val portBmp = remember(nov.portadaBase64) { ImageUtils.base64ToImageBitmap(nov.portadaBase64) }
                                        Surface(
                                            color = colors.cardBg,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(0.8.dp, colors.border),
                                            modifier = Modifier
                                                .width(235.dp)
                                                .clickable { onSeleccionarNovela(nov) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                PortadaConTextoSuperpuesto(
                                                    titulo = nov.titulo,
                                                    autor = nov.autor,
                                                    portadaBmp = portBmp,
                                                    colorHex = nov.portadaColorHex,
                                                    width = 48.dp,
                                                    height = 70.dp,
                                                    tituloSize = 8.sp,
                                                    autorSize = 6.sp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = nov.titulo,
                                                        fontFamily = FontFamily.Serif,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = colors.textDark,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "Cap. ${rel.capituloActual} • ${rel.progresoPorcentaje}%",
                                                        fontSize = 11.sp,
                                                        color = colors.textMuted
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    LinearProgressIndicator(
                                                        progress = { rel.progresoPorcentaje / 100f },
                                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                                        color = colors.purple
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (q.isEmpty() && generoSeleccionado == "Todos" && novelasRecomendadas.isNotEmpty()) {
                        item(key = "seccion_recomendadas") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Recomendadas para ti",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colors.textDark
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(novelasRecomendadas, key = { "rec_${it.id}" }) { novRec ->
                                        val portRec = remember(novRec.portadaBase64) { ImageUtils.base64ToImageBitmap(novRec.portadaBase64) }
                                        Surface(
                                            color = colors.cardBg,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(0.8.dp, colors.border),
                                            modifier = Modifier
                                                .width(118.dp)
                                                .clickable { onSeleccionarNovela(novRec) }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                PortadaConTextoSuperpuesto(
                                                    titulo = novRec.titulo,
                                                    autor = novRec.autor,
                                                    portadaBmp = portRec,
                                                    colorHex = novRec.portadaColorHex,
                                                    width = 96.dp,
                                                    height = 136.dp,
                                                    tituloSize = 11.sp,
                                                    autorSize = 8.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = novRec.titulo,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = colors.textDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = novRec.categoria,
                                                    fontSize = 10.sp,
                                                    color = colors.purple,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (q.isNotEmpty() && resultadosUsuarios.isNotEmpty()) {
                        item(key = "header_usuarios") {
                            Text(
                                text = "AUTORES Y LECTORES (${resultadosUsuarios.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.purple
                            )
                        }
                        items(resultadosUsuarios, key = { "user_${it.id}" }) { uItem ->
                            val loSigo = usuariosQueSigo.any { it.seguidoId == uItem.id }
                            val fotoUser = remember(uItem.fotoBase64) { ImageUtils.base64ToImageBitmap(uItem.fotoBase64) }
                            val esMiUsuario = uItem.id == usuario.id
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAbrirPerfilAutor(uItem.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically

                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(colors.bg)
                                            .border(1.dp, colors.purple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (fotoUser != null) {
                                            Image(bitmap = fotoUser, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        } else {
                                            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(uItem.usuario, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                        Text("Ver perfil, obras y enlaces", fontSize = 11.sp, color = colors.textMuted)
                                    }
                                    OutlinedButton(
                                        onClick = { FirestoreService.seguirODejarDeSeguirUsuario(usuario, uItem, loSigo) }
                                    ) {
                                        Text(if (loSigo) "Siguiendo" else "Seguir", color = colors.purple, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (filtradas.isNotEmpty()) {
                        item(key = "header_novelas") {
                            Text(
                                text = if (q.isNotEmpty()) "NOVELAS (${filtradas.size})" else "Explorar Novelas",
                                fontFamily = FontFamily.Serif,
                                fontSize = if (q.isNotEmpty()) 12.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (q.isNotEmpty()) colors.purple else colors.textDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    items(filtradas, key = { it.id }) { novela ->
                        val portadaBmp = remember(novela.portadaBase64) { ImageUtils.base64ToImageBitmap(novela.portadaBase64) }
                        Surface(
                            color = colors.cardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeleccionarNovela(novela) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                PortadaConTextoSuperpuesto(
                                    titulo = novela.titulo,
                                    autor = novela.autor,
                                    portadaBmp = portadaBmp,
                                    colorHex = novela.portadaColorHex,
                                    width = 68.dp,
                                    height = 98.dp,
                                    tituloSize = 10.sp,
                                    autorSize = 8.sp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(novela.categoria.uppercase(), fontSize = 10.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (novela.estaTerminada) Color(0xFF2E7D32).copy(alpha = 0.14f) else colors.purple.copy(alpha = 0.14f),
                                        border = BorderStroke(
                                            width = 0.8.dp,
                                            color = if (novela.estaTerminada) Color(0xFF2E7D32) else colors.purple
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = if (novela.estaTerminada) "✓ TERMINADA" else "⏳ EN EMISIÓN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (novela.estaTerminada) Color(0xFF2E7D32) else colors.purple,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                                    Text(
                                        text = "Por ${novela.autor}",
                                        fontSize = 12.sp,
                                        color = colors.purple,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable { onAbrirPerfilAutor(novela.autorId) }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(novela.sinopsis, fontSize = 12.sp, color = colors.textDark, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "♥ ${novela.corazones}  •  👍 ${novela.meGusta}  •  👎 ${novela.noMeGusta}  •  👁 ${novela.vistas}  •  💬 ${novela.totalComentarios}",
                                        fontSize = 10.sp,
                                        color = colors.purple,
                                        fontWeight = FontWeight.SemiBold
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

@Composable
fun PerfilPublicoAutorScreen(
    usuarioActual: UsuarioPerfil,
    autorId: String,
    novelas: List<Novela>,
    usuariosQueSigo: List<UsuarioSeguimiento>,
    onVolver: () -> Unit,
    onSeleccionarNovela: (Novela) -> Unit,
    onAbrirOtroPerfil: (String) -> Unit
) {
    BackHandler { onVolver() }

    val colors = LocalReadwoodColors.current
    val uriHandler = LocalUriHandler.current
    var perfilAutor by remember(autorId) { mutableStateOf<UsuarioPerfil?>(null) }
    var seguidosDelAutor by remember(autorId) { mutableStateOf(emptyList<UsuarioSeguimiento>()) }
    var mapaPerfiles by remember { mutableStateOf(emptyMap<String, UsuarioPerfil>()) }
    var mostrarDialogoReporte by remember { mutableStateOf(false) }
    var motivoReporte by remember { mutableStateOf("") }
    var mensajeAviso by remember { mutableStateOf("") }

    LaunchedEffect(autorId) {
        FirestoreService.obtenerPerfilPorId(autorId) { perfilAutor = it }
        FirestoreService.obtenerSeguidosPublicosDeUsuario(autorId) { seguidosDelAutor = it }
        FirestoreService.buscarUsuarios("") { lista ->
            mapaPerfiles = lista.associateBy { it.id }
        }
    }

    val obrasPublicadas = remember(novelas, autorId) {
        novelas.filter { it.autorId == autorId && it.esPublicada && it.id.isNotBlank() && it.titulo.isNotBlank() }
    }
    if (mostrarDialogoReporte && perfilAutor != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReporte = false },
            title = { Text("Reportar a ${perfilAutor!!.usuario}", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = motivoReporte,
                    onValueChange = { motivoReporte = it },
                    placeholder = { Text("Motivo del reporte...") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    FirestoreService.enviarReporte(
                        reportante = usuarioActual,
                        tipoObjetivo = "USUARIO",
                        objetivoId = perfilAutor!!.id,
                        objetivoNombre = perfilAutor!!.usuario,
                        motivo = motivoReporte.ifBlank { "Reporte de perfil" }
                    ) {
                        mensajeAviso = "Reporte enviado correctamente."
                    }
                    motivoReporte = ""
                    mostrarDialogoReporte = false
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoReporte = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        if (perfilAutor == null) {
            Box(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.purple)
            }
        } else {
            val autor = perfilAutor!!
            val esMiPropioPerfil = autor.id == usuarioActual.id
            val yaLoSigo = usuariosQueSigo.any { it.seguidoId == autor.id }
            val fotoBmp = remember(autor.fotoBase64) { ImageUtils.base64ToImageBitmap(autor.fotoBase64) }
            val bannerBmp = remember(autor.bannerBase64) { ImageUtils.base64ToImageBitmap(autor.bannerBase64) }
            val bannerColor = remember(autor.bannerColorHex) { parseHexColorSafe(autor.bannerColorHex, colors.purple) }

            Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(bannerColor)
                ) {
                    if (bannerBmp != null) {
                        Image(bitmap = bannerBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.45f),
                            modifier = Modifier.clickable { onVolver() }
                        ) {
                            Text("← Volver", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                        if (!esMiPropioPerfil) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.45f)
                            ) {
                                IconButton(onClick = { mostrarDialogoReporte = true }, modifier = Modifier.size(34.dp)) {
                                    IconoBocinaReporte(tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(2.5.dp, colors.bg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoBmp != null) {
                        Image(bitmap = fotoBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple, modifier = Modifier.size(52.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(autor.usuario, fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                        if (autor.mostrarCorreoPublico && autor.correo.isNotBlank()) {
                            Text("✉ ${autor.correo}", fontSize = 12.sp, color = colors.purple)
                        }
                    }

                    if (!esMiPropioPerfil) {
                        Button(
                            onClick = { FirestoreService.seguirODejarDeSeguirUsuario(usuarioActual, autor, yaLoSigo) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (yaLoSigo) colors.cardBg else colors.purple),
                            border = if (yaLoSigo) BorderStroke(1.dp, colors.purple) else null
                        ) {
                            Text(
                                text = if (yaLoSigo) "✓ Siguiendo" else "+ Seguir",
                                color = if (yaLoSigo) colors.purple else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (mensajeAviso.isNotEmpty()) {
                    Text(mensajeAviso, fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
                }

                Text("Obras publicadas (${obrasPublicadas.size})", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                if (obrasPublicadas.isEmpty()) {
                    Text("Este autor aún no tiene obras publicadas.", fontSize = 13.sp, color = colors.textMuted)
                } else {
                    obrasPublicadas.forEach { nov ->
                        val portBmp = remember(nov.portadaBase64) { ImageUtils.base64ToImageBitmap(nov.portadaBase64) }
                        Surface(
                            color = colors.cardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, colors.border),
                            modifier = Modifier.fillMaxWidth().clickable { onSeleccionarNovela(nov) }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                PortadaConTextoSuperpuesto(
                                    titulo = nov.titulo,
                                    autor = nov.autor,
                                    portadaBmp = portBmp,
                                    colorHex = nov.portadaColorHex,
                                    width = 52.dp,
                                    height = 74.dp,
                                    tituloSize = 8.sp,
                                    autorSize = 7.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nov.titulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                    Text("${nov.categoria} • ${nov.capitulos.size} cap(s)", fontSize = 11.sp, color = colors.purple)
                                    Text(nov.sinopsis, fontSize = 12.sp, color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.border)

                Text("Redes Sociales", fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                if (autor.enlacesRedes.isEmpty()) {
                    Text("Aún no hay redes", fontSize = 12.sp, color = colors.textMuted)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        autor.enlacesRedes.forEach { enlace ->
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val url = if (enlace.startsWith("http://") || enlace.startsWith("https://")) enlace else "https://$enlace"
                                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                                    }
                            ) {
                                Text(
                                    text = "🔗 $enlace",
                                    fontSize = 13.sp,
                                    color = colors.purple,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.border)

                Text("Personas que sigue (${seguidosDelAutor.size})", fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                if (seguidosDelAutor.isEmpty()) {
                    Text("No muestra usuarios seguidos.", fontSize = 12.sp, color = colors.textMuted)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(seguidosDelAutor, key = { it.id }) { seg ->
                            val perfSeg = mapaPerfiles[seg.seguidoId]
                            val fotoSeg = remember(perfSeg?.fotoBase64) { ImageUtils.base64ToImageBitmap(perfSeg?.fotoBase64 ?: "") }
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier.clickable { onAbrirOtroPerfil(seg.seguidoId) }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(30.dp).clip(CircleShape).background(colors.bg).border(1.dp, colors.purple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (fotoSeg != null) {
                                            Image(bitmap = fotoSeg, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        } else {
                                            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(seg.seguidoNombre, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textDark)
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
fun ComunidadScreen(
    usuario: UsuarioPerfil,
    novelas: List<Novela>,
    onSeleccionarNovela: (Novela) -> Unit,
    onAbrirPerfilAutor: (String) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var publicaciones by remember { mutableStateOf(emptyList<PublicacionComunidad>()) }
    var nuevoPost by remember { mutableStateOf("") }
    var imagenAdjuntaBase64 by remember { mutableStateOf("") }
    var fechaProgramadaMillis by remember { mutableStateOf<Long?>(null) }
    var mensajeErrorComunidad by remember { mutableStateOf("") }
    var ahoraMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val selectorImagenComunidad = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val b64 = ImageUtils.uriToCompressedBase64(context, uri)
            if (b64.isNotEmpty()) {
                imagenAdjuntaBase64 = b64
            }
        }
    }

    val hayContadoresActivos = remember(publicaciones, novelas) {
        publicaciones.any { it.fechaLanzamiento > ahoraMillis || it.timestamp > ahoraMillis } ||
                novelas.any { !it.esPublicada && it.fechaPublicacionProgramada > ahoraMillis }
    }

    LaunchedEffect(hayContadoresActivos) {
        while (hayContadoresActivos) {
            delay(1000L)
            ahoraMillis = System.currentTimeMillis()
        }
    }

    DisposableEffect(usuario.id) {
        val reg = FirestoreService.escucharComunidad(usuario.id) { publicaciones = it }
        onDispose { reg.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Comunidad Readwood",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )
        Text(
            text = "Anuncios de autores y lanzamientos programados",
            fontSize = 12.sp,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Caja para crear publicación con imagen y programación
        Surface(
            color = colors.cardBg,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nuevoPost,
                    onValueChange = { nuevoPost = it },
                    placeholder = {
                        Text(
                            text = "Comparte un aviso, adelanto o noticia con tus lectores...",
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Vista previa de imagen adjunta antes de publicar
                if (imagenAdjuntaBase64.isNotEmpty()) {
                    val previewBmp = remember(imagenAdjuntaBase64) {
                        ImageUtils.base64ToImageBitmap(imagenAdjuntaBase64)
                    }
                    if (previewBmp != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Image(
                                bitmap = previewBmp,
                                contentDescription = "Vista previa",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clickable { imagenAdjuntaBase64 = "" }
                            ) {
                                Text(
                                    text = "✕",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Botones para adjuntar imagen y programar fecha/hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { selectorImagenComunidad.launch("image/*") },
                        border = BorderStroke(1.dp, colors.purple),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            tint = colors.purple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (imagenAdjuntaBase64.isEmpty()) "Imagen" else "Cambiar img",
                            color = colors.purple,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            val elegida = Calendar.getInstance().apply {
                                                set(year, month, day, hour, minute, 0)
                                            }.timeInMillis
                                            if (elegida > System.currentTimeMillis()) {
                                                fechaProgramadaMillis = elegida
                                                mensajeErrorComunidad = ""
                                            } else {
                                                mensajeErrorComunidad = "Elige una fecha y hora futura."
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
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val etiquetaFecha = if (fechaProgramadaMillis == null) {
                            "⏳ Programar"
                        } else {
                            "⏳ " + SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(fechaProgramadaMillis!!))
                        }
                        Text(text = etiquetaFecha, color = colors.purple, fontSize = 12.sp)
                    }

                    if (fechaProgramadaMillis != null) {
                        TextButton(
                            onClick = { fechaProgramadaMillis = null },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Ahora", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                if (mensajeErrorComunidad.isNotEmpty()) {
                    Text(mensajeErrorComunidad, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (nuevoPost.isNotBlank() || imagenAdjuntaBase64.isNotEmpty()) {
                            val tsDestino = fechaProgramadaMillis ?: System.currentTimeMillis()
                            FirestoreService.publicarEnComunidad(
                                autor = usuario,
                                mensaje = nuevoPost,
                                imagenBase64 = imagenAdjuntaBase64,
                                timestampProgramado = tsDestino
                            ) {
                                nuevoPost = ""
                                imagenAdjuntaBase64 = ""
                                fechaProgramadaMillis = null
                                mensajeErrorComunidad = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (fechaProgramadaMillis != null) "Programar publicación" else "Publicar en Comunidad",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (publicaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay publicaciones en la comunidad.", color = colors.textMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                items(publicaciones, key = { it.id }) { post ->
                    val fotoAutor = remember(post.autorFotoBase64) {
                        ImageUtils.base64ToImageBitmap(post.autorFotoBase64)
                    }
                    val imagenPostBmp = remember(post.imagenBase64) {
                        ImageUtils.base64ToImageBitmap(post.imagenBase64)
                    }
                    val restanteMs = (post.fechaLanzamiento - ahoraMillis).coerceAtLeast(0L)
                    val esLanzamiento = post.novelaProgramadaId.isNotEmpty() && post.fechaLanzamiento > 0L
                    val esProgramadaFutura = post.timestamp > ahoraMillis
                    val esMiPost = post.autorId == usuario.id

                    Surface(
                        color = colors.cardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(enabled = !esMiPost) {
                                    if (!esMiPost) onAbrirPerfilAutor(post.autorId)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colors.bg)
                                        .border(1.dp, colors.purple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (fotoAutor != null) {
                                        Image(
                                            bitmap = fotoAutor,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${post.autorNombre} : Autor",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colors.purple
                                    )
                                    val subtitulo = if (esProgramadaFutura) {
                                        "Programada para: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp))
                                    } else {
                                        "Anuncio oficial"
                                    }
                                    Text(subtitulo, fontSize = 11.sp, color = colors.textMuted)
                                }
                            }

                            if (post.mensaje.isNotBlank()) {
                                Text(post.mensaje, fontSize = 14.sp, color = colors.textDark)
                            }

                            // Imagen adjunta de la publicación
                            if (imagenPostBmp != null) {
                                Image(
                                    bitmap = imagenPostBmp,
                                    contentDescription = "Imagen de comunidad",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }

                            if (esLanzamiento) {
                                Surface(
                                    color = colors.purple.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, colors.purple),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "ESTRENO PROGRAMADO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.purple
                                        )
                                        Text(
                                            text = post.novelaProgramadaTitulo,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = colors.textDark
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (restanteMs > 0L) {
                                            val totalSeg = restanteMs / 1000
                                            val dias = totalSeg / 86400
                                            val horas = (totalSeg % 86400) / 3600
                                            val mins = (totalSeg % 3600) / 60
                                            val segs = totalSeg % 60
                                            val formato = if (dias > 0) {
                                                String.format("%dd %02d:%02d:%02d", dias, horas, mins, segs)
                                            } else {
                                                String.format("%02d:%02d:%02d", horas, mins, segs)
                                            }
                                            Text(
                                                text = "⏳ Se publica en: $formato",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.purple
                                            )
                                        } else {
                                            Button(
                                                onClick = {
                                                    val nov = novelas.find { it.id == post.novelaProgramadaId }
                                                    if (nov != null) onSeleccionarNovela(nov)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                                            ) {
                                                Text(
                                                    text = "📖 ¡Ya disponible! Leer ahora",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
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
        }
    }
}

fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun BibliotecaScreen(
    novelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    usuariosQueSigo: List<UsuarioSeguimiento>,
    onSeleccionarNovela: (Novela) -> Unit,
    onAbrirPerfilAutor: (String) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current
    var tabIndex by remember { mutableIntStateOf(0) }
    var offlineList by remember { mutableStateOf(OfflineStorage.obtenerNovelasOffline(context)) }
    var mapaUsuarios by remember { mutableStateOf(emptyMap<String, UsuarioPerfil>()) }

    LaunchedEffect(usuariosQueSigo) {
        if (usuariosQueSigo.isNotEmpty()) {
            FirestoreService.buscarUsuarios("") { lista ->
                mapaUsuarios = lista.associateBy { it.id }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text("Mi Biblioteca", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
        Text("Toca el ícono de ojo para mostrar u ocultar de tu perfil público", fontSize = 11.sp, color = colors.textMuted)
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = tabIndex, containerColor = colors.cardBg, contentColor = colors.purple) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Novelas (${seguimientos.size})") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Usuarios (${usuariosQueSigo.size})") })
            Tab(selected = tabIndex == 2, onClick = {
                offlineList = OfflineStorage.obtenerNovelasOffline(context)
                tabIndex = 2
            }, text = { Text("Offline (${offlineList.size})") })
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (tabIndex) {
            0 -> {
                if (seguimientos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes novelas seguidas.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        items(seguimientos, key = { it.id }) { rel ->
                            val novela = novelas.find { it.id == rel.novelaId }
                            val portadaBmp = remember(novela?.portadaBase64) {
                                ImageUtils.base64ToImageBitmap(novela?.portadaBase64 ?: "")
                            }

                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (novela != null) onSeleccionarNovela(novela) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PortadaConTextoSuperpuesto(
                                        titulo = rel.novelaTitulo,
                                        autor = rel.seguidoAutorNombre,
                                        portadaBmp = portadaBmp,
                                        colorHex = novela?.portadaColorHex ?: "#5E2D40",
                                        width = 58.dp,
                                        height = 84.dp,
                                        tituloSize = 9.sp,
                                        autorSize = 7.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(rel.novelaTitulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                                Text("Autor: ${rel.seguidoAutorNombre}", fontSize = 12.sp, color = colors.purple)
                                                Text(
                                                    text = if (rel.oculta) "Oculta para otros usuarios" else "Visible en tu perfil",
                                                    fontSize = 11.sp,
                                                    color = colors.textMuted
                                                )
                                            }
                                            IconButton(onClick = { FirestoreService.alternarOcultarSeguimiento(rel.id, !rel.oculta) }) {
                                                IconoOjoVisibilidad(
                                                    oculto = rel.oculta,
                                                    tint = if (rel.oculta) colors.textMuted else colors.purple
                                                )
                                            }
                                            IconButton(onClick = { FirestoreService.eliminarSeguimiento(rel.id) }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        LinearProgressIndicator(
                                            progress = { rel.progresoPorcentaje / 100f },
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                            color = colors.purple
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                if (usuariosQueSigo.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aún no sigues a otros usuarios.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        items(usuariosQueSigo, key = { it.id }) { segUser ->
                            val perfilSeguido = mapaUsuarios[segUser.seguidoId]
                            val fotoSeguido = remember(perfilSeguido?.fotoBase64) {
                                ImageUtils.base64ToImageBitmap(perfilSeguido?.fotoBase64 ?: "")
                            }

                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAbrirPerfilAutor(segUser.seguidoId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(colors.bg)
                                            .border(1.dp, colors.purple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (fotoSeguido != null) {
                                            Image(bitmap = fotoSeguido, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        } else {
                                            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple, modifier = Modifier.size(28.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(segUser.seguidoNombre, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                        Text(
                                            text = if (segUser.oculta) "Oculto en tu perfil público" else "Visible en tu perfil público",
                                            fontSize = 11.sp,
                                            color = colors.textMuted
                                        )
                                    }

                                    IconButton(onClick = {
                                        FirestoreService.alternarOcultarUsuarioSeguido(segUser.id, !segUser.oculta)
                                    }) {
                                        IconoOjoVisibilidad(
                                            oculto = segUser.oculta,
                                            tint = if (segUser.oculta) colors.textMuted else colors.purple
                                        )
                                    }

                                    IconButton(onClick = {
                                        FirestoreService.seguirODejarDeSeguirUsuario(
                                            seguidor = UsuarioPerfil(id = segUser.seguidorId, usuario = segUser.seguidorNombre),
                                            objetivo = UsuarioPerfil(id = segUser.seguidoId, usuario = segUser.seguidoNombre),
                                            yaLoSigue = true
                                        )
                                    }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Dejar de seguir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                if (offlineList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay novelas guardadas para lectura interna.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 28.dp)
                    ) {
                        items(offlineList, key = { it.id }) { novOff ->
                            val portadaOff = remember(novOff.portadaBase64) {
                                ImageUtils.base64ToImageBitmap(novOff.portadaBase64)
                            }
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeleccionarNovela(novOff) }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    PortadaConTextoSuperpuesto(
                                        titulo = novOff.titulo,
                                        autor = novOff.autor,
                                        portadaBmp = portadaOff,
                                        colorHex = novOff.portadaColorHex,
                                        width = 52.dp,
                                        height = 76.dp,
                                        tituloSize = 8.sp,
                                        autorSize = 7.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(novOff.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                        Text("${novOff.capitulos.size} capítulo(s) guardados en la app", fontSize = 12.sp, color = colors.purple)
                                    }
                                    IconButton(onClick = {
                                        OfflineStorage.eliminarNovelaOffline(context, novOff.id)
                                        offlineList = OfflineStorage.obtenerNovelasOffline(context)
                                    }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Quitar de la app", tint = MaterialTheme.colorScheme.error)
                                    }
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
fun DetalleNovelaScreen(
    usuario: UsuarioPerfil,
    novela: Novela,
    seguimientos: List<SeguimientoRelacion>,
    novelas: List<Novela> = emptyList(),
    onSeleccionarNovela: (Novela) -> Unit = {},
    onVolver: () -> Unit,
    onLeerCapitulo: (Int) -> Unit,
    onAbrirPerfilAutor: (String) -> Unit,
    onAgregarCapituloANovela: (Novela) -> Unit
) {
    BackHandler { onVolver() }

    val colors = LocalReadwoodColors.current
    val context = LocalContext.current
    val relacion = seguimientos.find { it.novelaId == novela.id }
    val esMiPropiaNovela = novela.autorId == usuario.id
    var mensajeBanner by remember { mutableStateOf("") }

    // Estados para reportar la novela con motivos seleccionables + "Otro motivo"
    var mostrarDialogoReporteNovela by remember { mutableStateOf(false) }
    val motivosPredefinidos = listOf(
        "Contenido inapropiado o explícito",
        "Plagio o robo de obra",
        "Discurso de odio o acoso",
        "Spam o publicidad engañosa",
        "Información falsa o engañosa",
        "Otro motivo"
    )
    var motivoSeleccionado by remember { mutableStateOf(motivosPredefinidos.first()) }
    var otroMotivoTexto by remember { mutableStateOf("") }

    // Recomendaciones al final de la novela:
    // Mismo autor, o mínimo 1 etiqueta en común, o afinidad con los gustos del usuario
    val novelasRecomendadasDetalle = remember(novelas, novela.id, seguimientos) {
        val tagsNovelaActual = novela.etiquetas.map { it.lowercase() }.toSet()
        val idsLeidas = seguimientos.map { it.novelaId }.toSet()
        val novelasQueLeeUsuario = novelas.filter { it.id in idsLeidas }
        val generosUsuario = novelasQueLeeUsuario.map { it.categoria.lowercase() }.toSet()
        val tagsUsuario = novelasQueLeeUsuario.flatMap { n -> n.etiquetas.map { it.lowercase() } }.toSet()
        val autoresUsuario = novelasQueLeeUsuario.map { it.autorId }.toSet()

        novelas
            .filter { it.esPublicada && it.id != novela.id }
            .map { cand ->
                val esMismoAutor = cand.autorId == novela.autorId
                val tagsCand = cand.etiquetas.map { it.lowercase() }
                val tieneEtiquetaComun = tagsCand.any { it in tagsNovelaActual }
                val coincideConGustosUsuario =
                    cand.categoria.lowercase() in generosUsuario ||
                            cand.autorId in autoresUsuario ||
                            tagsCand.any { it in tagsUsuario }

                var puntaje = 0
                if (esMismoAutor) puntaje += 5
                if (tieneEtiquetaComun) puntaje += 4
                if (coincideConGustosUsuario) puntaje += 2

                val motivo = when {
                    esMismoAutor -> "Mismo autor"
                    tieneEtiquetaComun -> "Etiquetas en común"
                    else -> cand.categoria
                }
                Triple(cand, puntaje, motivo)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(8)
    }

    if (mostrarDialogoReporteNovela) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoReporteNovela = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Reportar \"${novela.titulo}\"",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.textDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Selecciona el motivo del reporte:",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    motivosPredefinidos.forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { motivoSeleccionado = opcion }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = motivoSeleccionado == opcion,
                                onClick = { motivoSeleccionado = opcion },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.purple)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = opcion,
                                fontSize = 13.sp,
                                color = colors.textDark
                            )
                        }
                    }

                    if (motivoSeleccionado == "Otro motivo") {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = otroMotivoTexto,
                            onValueChange = { otroMotivoTexto = it },
                            placeholder = { Text("Escribe aquí el motivo...", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val motivoFinal = if (motivoSeleccionado == "Otro motivo") {
                            otroMotivoTexto.trim().ifBlank { "Otro motivo no especificado" }
                        } else {
                            motivoSeleccionado
                        }
                        FirestoreService.enviarReporte(
                            reportante = usuario,
                            tipoObjetivo = "NOVELA",
                            objetivoId = novela.id,
                            objetivoNombre = novela.titulo,
                            motivo = motivoFinal
                        ) {
                            mensajeBanner = "Reporte enviado correctamente."
                        }
                        otroMotivoTexto = ""
                        motivoSeleccionado = motivosPredefinidos.first()
                        mostrarDialogoReporteNovela = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Enviar reporte", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoReporteNovela = false }) {
                    Text("Cancelar", color = colors.purple)
                }
            }
        )
    }

    val portadaBmp = remember(novela.portadaBase64) { ImageUtils.base64ToImageBitmap(novela.portadaBase64) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onVolver,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            PortadaConTextoSuperpuesto(
                titulo = novela.titulo,
                autor = novela.autor,
                portadaBmp = portadaBmp,
                colorHex = novela.portadaColorHex,
                width = 98.dp,
                height = 142.dp,
                tituloSize = 13.sp,
                autorSize = 10.sp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(novela.categoria.uppercase(), fontSize = 11.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (novela.estaTerminada) Color(0xFF2E7D32).copy(alpha = 0.14f) else colors.purple.copy(alpha = 0.14f),
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (novela.estaTerminada) Color(0xFF2E7D32) else colors.purple
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = if (novela.estaTerminada) "✓ TERMINADA" else "⏳ EN EMISIÓN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (novela.estaTerminada) Color(0xFF2E7D32) else colors.purple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                Text(
                    text = "Por ${novela.autor} (Ver perfil)",
                    fontSize = 13.sp,
                    color = colors.purple,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onAbrirPerfilAutor(novela.autorId) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "♥ ${novela.corazones}  •  👍 ${novela.meGusta}  •  👎 ${novela.noMeGusta}\n👁 ${novela.vistas} Vistas  •  💬 ${novela.totalComentarios} Comentarios",
                    fontSize = 11.sp,
                    color = colors.purple,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(novela.sinopsis, fontSize = 14.sp, color = colors.textDark)

                if (novela.etiquetas.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(novela.etiquetas) { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.cardBg,
                                border = BorderStroke(0.8.dp, colors.border)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 11.sp,
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!esMiPropiaNovela) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = colors.cardBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f)),
                    modifier = Modifier.clickable { mostrarDialogoReporteNovela = true }
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconoBocinaReporte(
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (!esMiPropiaNovela) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (relacion == null) {
                            FirestoreService.seguirNovelaYAutor(usuario, novela, ocultarDeOtros = false) {
                                mensajeBanner = "Ahora sigues esta novela y a ${novela.autor}"
                            }
                        } else {
                            FirestoreService.alternarOcultarSeguimiento(relacion.id, !relacion.oculta)
                            mensajeBanner = if (!relacion.oculta) "Novela ocultada de tu perfil público" else "Novela visible en tu lista"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when {
                            relacion == null -> "+ Seguir novela"
                            relacion.oculta -> "Oculta (Mostrar)"
                            else -> "✓ Siguiendo (Ocultar)"
                        },
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = {
                        OfflineStorage.guardarOfflineEnApp(context, novela, null)
                        mensajeBanner = "Novela completa guardada solo dentro de la app"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar en App", fontSize = 12.sp, color = colors.purple)
                }
            }
        }

        if (mensajeBanner.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(mensajeBanner, fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Capítulos (${novela.capitulos.size})", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
        Spacer(modifier = Modifier.height(8.dp))

        novela.capitulos.forEachIndexed { index, cap ->
            Surface(
                color = colors.cardBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onLeerCapitulo(index) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Capítulo ${cap.numero}: ${cap.titulo}", fontWeight = FontWeight.Bold, color = colors.textDark)
                        if (cap.esBorrador) {
                            Text("BORRADOR PRIVADO (Solo visible para ti)", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                    if (!esMiPropiaNovela) {
                        TextButton(onClick = {
                            OfflineStorage.guardarOfflineEnApp(context, novela, cap)
                            mensajeBanner = "Capítulo '${cap.titulo}' guardado en la app"
                        }) {
                            Text("Guardar Cap.", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                }
            }
        }

        if (novelasRecomendadasDetalle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))
            HorizontalDivider(color = colors.border)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "También te puede gustar",
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(novelasRecomendadasDetalle, key = { "det_rec_${it.first.id}" }) { (novRec, _, motivo) ->
                    val portRec = remember(novRec.portadaBase64) {
                        ImageUtils.base64ToImageBitmap(novRec.portadaBase64)
                    }
                    Surface(
                        color = colors.cardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .width(118.dp)
                            .clickable { onSeleccionarNovela(novRec) }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PortadaConTextoSuperpuesto(
                                titulo = novRec.titulo,
                                autor = novRec.autor,
                                portadaBmp = portRec,
                                colorHex = novRec.portadaColorHex,
                                width = 96.dp,
                                height = 136.dp,
                                tituloSize = 11.sp,
                                autorSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = novRec.titulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.textDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = motivo,
                                fontSize = 10.sp,
                                color = colors.purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
@Composable
fun LecturaScreen(
    usuario: UsuarioPerfil,
    novela: Novela,
    indiceCapituloInicial: Int = 0,
    seguimientos: List<SeguimientoRelacion>,
    modoOscuroApp: Boolean = false,
    novelas: List<Novela> = emptyList(),
    onSeleccionarNovela: (Novela) -> Unit = {},
    onVolver: () -> Unit,
    onAbrirPerfilAutor: (String) -> Unit
) {
    BackHandler { onVolver() }

    val scrollState = rememberScrollState()
    var indiceCap by remember { mutableIntStateOf(indiceCapituloInicial.coerceIn(0, (novela.capitulos.size - 1).coerceAtLeast(0))) }

    var temaActual by remember(modoOscuroApp) {
        mutableStateOf(if (modoOscuroApp) ConfigTemas.Noche else ConfigTemas.Sepia)
    }
    var tamanoTexto by remember { mutableFloatStateOf(18f) }

    val esMiPropiaNovela = novela.autorId == usuario.id
    var vistaRegistradaLocal by remember(novela.id, indiceCap) { mutableStateOf(false) }

    val capituloActual = novela.capitulos.getOrNull(indiceCap)
    var comentarios by remember { mutableStateOf(emptyList<ComentarioItem>()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var comentarioAReportar by remember { mutableStateOf<ComentarioItem?>(null) }
    var motivoReporteComentario by remember { mutableStateOf("") }
    var avisoReporte by remember(indiceCap) { mutableStateOf("") }

    val yaDioCorazon = capituloActual?.usuariosCorazon?.contains(usuario.id) == true
    val yaDioLike = capituloActual?.usuariosLike?.contains(usuario.id) == true
    val yaDioDislike = capituloActual?.usuariosDislike?.contains(usuario.id) == true

    LaunchedEffect(indiceCap) {
        scrollState.scrollTo(0)
    }

    DisposableEffect(novela.id, capituloActual?.id) {
        val reg = if (capituloActual != null) {
            FirestoreService.escucharComentarios(novela.id, capituloActual.id) { comentarios = it }
        } else null
        onDispose { reg?.remove() }
    }

    val porcentajeLectura = if (scrollState.maxValue > 0) {
        ((scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 100

    LaunchedEffect(porcentajeLectura, indiceCap) {
        if (porcentajeLectura >= 100 && !vistaRegistradaLocal && capituloActual != null && !esMiPropiaNovela) {
            vistaRegistradaLocal = true
            FirestoreService.incrementarVistaUnicaCapitulo(novela, capituloActual.id, usuario.id)
        }
        val rel = seguimientos.find { it.novelaId == novela.id }
        if (rel != null && porcentajeLectura > 0) {
            FirestoreService.actualizarProgresoLectura(rel.id, indiceCap + 1, porcentajeLectura)
        }
    }

    if (comentarioAReportar != null) {
        val com = comentarioAReportar!!
        AlertDialog(
            onDismissRequest = { comentarioAReportar = null },
            title = { Text("Reportar comentario de ${com.usuarioNombre}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\"${com.texto}\"", fontSize = 13.sp)
                    OutlinedTextField(
                        value = motivoReporteComentario,
                        onValueChange = { motivoReporteComentario = it },
                        placeholder = { Text("Motivo del reporte...") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    FirestoreService.enviarReporte(
                        reportante = usuario,
                        tipoObjetivo = "COMENTARIO",
                        objetivoId = com.id,
                        objetivoNombre = com.usuarioNombre,
                        motivo = motivoReporteComentario.ifBlank { "Comentario inapropiado" }
                    ) {
                        avisoReporte = "Comentario reportado correctamente."
                    }
                    motivoReporteComentario = ""
                    comentarioAReportar = null
                }) {
                    Text("Enviar reporte")
                }
            },
            dismissButton = {
                TextButton(onClick = { comentarioAReportar = null }) { Text("Cancelar") }
            }
        )
    }

    var arrastreHorizontal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(temaActual.fondo)
            .navigationBarsPadding()
            .pointerInput(indiceCap, novela.capitulos.size) {
                detectHorizontalDragGestures(
                    onDragStart = { arrastreHorizontal = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        arrastreHorizontal += dragAmount
                    },
                    onDragEnd = {
                        if (arrastreHorizontal < -130f && indiceCap < novela.capitulos.lastIndex) {
                            indiceCap++
                        } else if (arrastreHorizontal > 130f && indiceCap > 0) {
                            indiceCap--
                        }
                        arrastreHorizontal = 0f
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onVolver) {
                Text("← Volver", color = temaActual.texto, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(ConfigTemas.Sepia, ConfigTemas.Noche, ConfigTemas.Mistico, ConfigTemas.Claro).forEach { tema ->
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(tema.fondo)
                            .clickable { temaActual = tema }
                    )
                }
                TextButton(onClick = { tamanoTexto = (tamanoTexto - 2f).coerceAtLeast(14f) }) { Text("A-", color = temaActual.texto) }
                TextButton(onClick = { tamanoTexto = (tamanoTexto + 2f).coerceAtMost(28f) }) { Text("A+", color = temaActual.texto) }
            }
            Text("$porcentajeLectura%", color = temaActual.acento, fontWeight = FontWeight.Bold)
        }

        LinearProgressIndicator(
            progress = { porcentajeLectura / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = temaActual.acento
        )

        if (capituloActual == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Esta novela aún no tiene capítulos publicados.", color = temaActual.texto)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                EncabezadoOrnamental(
                    genero = novela.categoria,
                    tituloNovela = novela.titulo,
                    tituloCapitulo = "Capítulo ${capituloActual.numero}: ${capituloActual.titulo}",
                    colorAcento = temaActual.acento,
                    colorTexto = temaActual.texto
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextoCapitularDecorado(
                    contenido = capituloActual.contenido,
                    colorAcento = temaActual.acento,
                    colorTexto = temaActual.texto,
                    tamanoTexto = tamanoTexto
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = temaActual.texto.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (indiceCap > 0) {
                        TextButton(onClick = { indiceCap-- }) {
                            Text("← Anterior", color = temaActual.texto, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (indiceCap < novela.capitulos.lastIndex) {
                        Button(
                            onClick = { indiceCap++ },
                            colors = ButtonDefaults.buttonColors(containerColor = temaActual.acento)
                        ) {
                            Text("Siguiente →", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (indiceCap == novela.capitulos.lastIndex) {
                    val recomendacionesFin = remember(novelas, novela.id, seguimientos) {
                        val tagsActuales = novela.etiquetas.map { it.lowercase() }.toSet()
                        val idsLeidas = seguimientos.map { it.novelaId }.toSet()
                        val novelasUser = novelas.filter { it.id in idsLeidas }
                        val generosUser = novelasUser.map { it.categoria.lowercase() }.toSet()
                        val tagsUser = novelasUser.flatMap { n -> n.etiquetas.map { it.lowercase() } }.toSet()

                        novelas
                            .filter { it.esPublicada && it.id != novela.id }
                            .map { cand ->
                                var pts = 0
                                if (cand.autorId == novela.autorId) pts += 5
                                val tagsCand = cand.etiquetas.map { it.lowercase() }
                                if (tagsCand.any { it in tagsActuales }) pts += 4
                                if (cand.categoria.lowercase() in generosUser || tagsCand.any { it in tagsUser }) pts += 2
                                cand to pts
                            }
                            .filter { it.second > 0 }
                            .sortedByDescending { it.second }
                            .take(6)
                            .map { it.first }
                    }

                    if (recomendacionesFin.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Has llegado al último capítulo • Recomendadas para ti",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = temaActual.acento
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recomendacionesFin, key = { "fin_${it.id}" }) { novRec ->
                                val portRec = remember(novRec.portadaBase64) {
                                    ImageUtils.base64ToImageBitmap(novRec.portadaBase64)
                                }
                                Surface(
                                    color = temaActual.texto.copy(alpha = 0.07f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.8.dp, temaActual.acento.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .width(115.dp)
                                        .clickable { onSeleccionarNovela(novRec) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        PortadaConTextoSuperpuesto(
                                            titulo = novRec.titulo,
                                            autor = novRec.autor,
                                            portadaBmp = portRec,
                                            colorHex = novRec.portadaColorHex,
                                            width = 92.dp,
                                            height = 130.dp,
                                            tituloSize = 10.sp,
                                            autorSize = 8.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = novRec.titulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = temaActual.texto,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (novRec.autorId == novela.autorId) "Mismo autor" else novRec.categoria,
                                            fontSize = 9.sp,
                                            color = temaActual.acento,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comentarios (${comentarios.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = temaActual.texto
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                FirestoreService.registrarReaccionCapitulo(
                                    novela = novela,
                                    capituloId = capituloActual.id,
                                    usuarioId = usuario.id,
                                    tipo = "CORAZON"
                                ) { aviso -> avisoReporte = aviso }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (yaDioCorazon) Color(0xFFD81B60).copy(alpha = 0.20f) else Color.Transparent
                            ),
                            border = BorderStroke(if (yaDioCorazon) 1.5.dp else 1.dp, Color(0xFFD81B60)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (yaDioCorazon) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Me encanta",
                                tint = Color(0xFFD81B60),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${capituloActual.corazones}", color = Color(0xFFD81B60), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                FirestoreService.registrarReaccionCapitulo(
                                    novela = novela,
                                    capituloId = capituloActual.id,
                                    usuarioId = usuario.id,
                                    tipo = "LIKE"
                                ) { aviso -> avisoReporte = aviso }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (yaDioLike) Color(0xFF1976D2).copy(alpha = 0.20f) else Color.Transparent
                            ),
                            border = BorderStroke(if (yaDioLike) 1.5.dp else 1.dp, Color(0xFF1976D2)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (yaDioLike) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${capituloActual.meGusta}", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                FirestoreService.registrarReaccionCapitulo(
                                    novela = novela,
                                    capituloId = capituloActual.id,
                                    usuarioId = usuario.id,
                                    tipo = "DISLIKE"
                                ) { aviso -> avisoReporte = aviso }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (yaDioDislike) Color(0xFFD32F2F).copy(alpha = 0.20f) else Color.Transparent
                            ),
                            border = BorderStroke(if (yaDioDislike) 1.5.dp else 1.dp, Color(0xFFD32F2F)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👎 ${capituloActual.noMeGusta}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (avisoReporte.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(avisoReporte, fontSize = 12.sp, color = temaActual.acento, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        placeholder = { Text("Escribe un comentario sobre este capítulo...", color = Color.DarkGray) },
                        textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        FirestoreService.publicarComentario(novela, capituloActual.id, usuario, nuevoComentario)
                        nuevoComentario = ""
                    }) {
                        Text("Enviar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                comentarios.forEach { com ->
                    val fotoComentario = remember(com.usuarioFotoBase64) {
                        ImageUtils.base64ToImageBitmap(com.usuarioFotoBase64)
                    }
                    val esAutorDeLaObra = com.usuarioId == novela.autorId

                    Card(
                        colors = CardDefaults.cardColors(containerColor = temaActual.texto.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(temaActual.fondo)
                                    .border(1.dp, temaActual.acento, CircleShape)
                                    .clickable { onAbrirPerfilAutor(com.usuarioId) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (fotoComentario != null) {
                                    Image(bitmap = fotoComentario, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = temaActual.acento, modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (esAutorDeLaObra) "${com.usuarioNombre} : Autor" else com.usuarioNombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = temaActual.acento,
                                    modifier = Modifier.clickable { onAbrirPerfilAutor(com.usuarioId) }
                                )
                                Text(com.texto, fontSize = 14.sp, color = temaActual.texto)
                            }
                            if (com.usuarioId != usuario.id) {
                                IconButton(onClick = { comentarioAReportar = com }) {
                                    IconoBocinaReporte(tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}