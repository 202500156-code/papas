package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: UsuarioPerfil,
    novelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    usuariosQueSigo: List<UsuarioSeguimiento>,
    notificacionesSinLeer: Int,
    onAbrirNotificaciones: () -> Unit,
    onSeleccionarNovela: (Novela) -> Unit
) {
    val colors = LocalReadwoodColors.current
    var modoBusqueda by remember { mutableIntStateOf(0) } // 0 = Obras, 1 = Usuarios
    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Todos") }
    val generos = listOf("Todos", "Fantasía", "Romance", "Misterio", "Terror", "Ciencia Ficción")

    // Estados para búsqueda y vista de perfil de otros usuarios
    var resultadosUsuarios by remember { mutableStateOf<List<UsuarioPerfil>>(emptyList()) }
    var usuarioInspeccionado by remember { mutableStateOf<UsuarioPerfil?>(null) }
    var seguidosDelUsuario by remember { mutableStateOf<List<UsuarioSeguimiento>>(emptyList()) }
    var novelasVistasDelUsuario by remember { mutableStateOf<List<SeguimientoRelacion>>(emptyList()) }

    LaunchedEffect(modoBusqueda, textoBusqueda) {
        if (modoBusqueda == 1) {
            FirestoreService.buscarUsuarios(textoBusqueda) { lista ->
                resultadosUsuarios = lista.filter { it.id != usuario.id }
            }
        }
    }

    // Modal de Perfil Público de otro Usuario (con bocina de reporte, botón seguir, usuarios seguidos y novelas vistas)
    if (usuarioInspeccionado != null) {
        val uObj = usuarioInspeccionado!!
        val yaLoSigo = usuariosQueSigo.any { it.seguidoId == uObj.id }
        val fotoAjena = remember(uObj.fotoBase64) { ImageUtils.base64ToImageBitmap(uObj.fotoBase64) }
        val obrasDelUsuario = novelas.filter { it.autorId == uObj.id && it.esPublicada }

        LaunchedEffect(uObj.id) {
            FirestoreService.obtenerActividadPublicaUsuario(uObj.id) { users, novs ->
                seguidosDelUsuario = users
                novelasVistasDelUsuario = novs
            }
        }

        AlertDialog(
            onDismissRequest = { usuarioInspeccionado = null },
            containerColor = colors.cardBg,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.bg)
                                .border(1.dp, colors.purple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoAjena != null) {
                                Image(bitmap = fotoAjena, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(uObj.usuario, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.textDark)
                    }
                    IconButton(onClick = {
                        FirestoreService.enviarReporte(
                            reportante = usuario,
                            tipoObjetivo = "USUARIO",
                            objetivoId = uObj.id,
                            objetivoNombre = uObj.usuario,
                            motivo = "Reporte desde perfil de lector/escritor"
                        )
                        usuarioInspeccionado = null
                    }) {
                        IconoBocinaReporte(tint = MaterialTheme.colorScheme.error)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            FirestoreService.seguirODejarDeSeguirUsuario(usuario, uObj, yaLoSigo)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (yaLoSigo) colors.bg else colors.purple
                        ),
                        border = if (yaLoSigo) BorderStroke(1.dp, colors.purple) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (yaLoSigo) "✓ Siguiendo usuario (Dejar de seguir)" else "+ Seguir usuario",
                            color = if (yaLoSigo) colors.purple else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uObj.consintioRedes && (uObj.redInstagram.isNotBlank() || uObj.redTwitter.isNotBlank())) {
                        Text(
                            text = "Redes: ${listOf(uObj.redInstagram, uObj.redTwitter).filter { it.isNotBlank() }.joinToString(" • ")}",
                            fontSize = 12.sp,
                            color = colors.purple
                        )
                    }

                    HorizontalDivider(color = colors.border)

                    // 1. Usuarios que sigue (respeta privacidad ocultarSeguidos)
                    Text("Usuarios que sigue:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                    if (uObj.ocultarSeguidos) {
                        Text("🔒 Este usuario ocultó a quién sigue.", fontSize = 12.sp, color = colors.textMuted)
                    } else if (seguidosDelUsuario.isEmpty()) {
                        Text("Aún no sigue a otros usuarios.", fontSize = 12.sp, color = colors.textMuted)
                    } else {
                        Text(
                            text = seguidosDelUsuario.joinToString(", ") { it.seguidoNombre },
                            fontSize = 12.sp,
                            color = colors.textDark
                        )
                    }

                    HorizontalDivider(color = colors.border)

                    // 2. Novelas que sigue / ve (respeta privacidad ocultarNovelasVistas y novelas ocultas individuales)
                    Text("Novelas que sigue y lee:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                    if (uObj.ocultarNovelasVistas) {
                        Text("🔒 Este usuario ocultó las novelas que lee.", fontSize = 12.sp, color = colors.textMuted)
                    } else if (novelasVistasDelUsuario.isEmpty()) {
                        Text("No tiene novelas públicas en su lista de lectura.", fontSize = 12.sp, color = colors.textMuted)
                    } else {
                        novelasVistasDelUsuario.forEach { rel ->
                            Text(
                                text = "• ${rel.novelaTitulo} (${rel.progresoPorcentaje}% leído)",
                                fontSize = 12.sp,
                                color = colors.purple,
                                modifier = Modifier.clickable {
                                    val nov = novelas.find { it.id == rel.novelaId }
                                    if (nov != null) {
                                        usuarioInspeccionado = null
                                        onSeleccionarNovela(nov)
                                    }
                                }
                            )
                        }
                    }

                    if (obrasDelUsuario.isNotEmpty()) {
                        HorizontalDivider(color = colors.border)
                        Text("Novelas publicadas por ${uObj.usuario}:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                        obrasDelUsuario.forEach { nov ->
                            Text(
                                text = "📖 ${nov.titulo}",
                                fontSize = 12.sp,
                                color = colors.purple,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    usuarioInspeccionado = null
                                    onSeleccionarNovela(nov)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { usuarioInspeccionado = null }) {
                    Text("Cerrar", color = colors.purple)
                }
            }
        )
    }

    val novelasPublicadas = novelas.filter { it.esPublicada }
    val filtradas = novelasPublicadas.filter { n ->
        (n.titulo.contains(textoBusqueda, true) || n.autor.contains(textoBusqueda, true)) &&
                (generoSeleccionado == "Todos" || n.categoria.contains(generoSeleccionado, true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                            fontSize = 24.sp
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
            // Selector limpio entre Obras y Usuarios
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (modoBusqueda == 0) colors.purple else colors.cardBg,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.weight(1f).clickable { modoBusqueda = 0 }
                ) {
                    Text(
                        text = "Novelas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (modoBusqueda == 0) Color.White else colors.textDark,
                        modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (modoBusqueda == 1) colors.purple else colors.cardBg,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.weight(1f).clickable { modoBusqueda = 1 }
                ) {
                    Text(
                        text = "Lectores y Autores",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (modoBusqueda == 1) Color.White else colors.textDark,
                        modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        if (modoBusqueda == 0) "Buscar novelas o autores..." else "Buscar usuario por nombre...",
                        color = colors.textMuted
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (modoBusqueda == 1) {
                // Lista de usuarios encontrados para seguir y ver su actividad
                if (resultadosUsuarios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron otros usuarios con ese nombre.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(resultadosUsuarios, key = { it.id }) { uItem ->
                            val loSigo = usuariosQueSigo.any { it.seguidoId == uItem.id }
                            val fotoUser = remember(uItem.fotoBase64) { ImageUtils.base64ToImageBitmap(uItem.fotoBase64) }
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { usuarioInspeccionado = uItem }
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
                                        Text("Toca para ver a quién sigue y qué lee", fontSize = 11.sp, color = colors.textMuted)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            FirestoreService.seguirODejarDeSeguirUsuario(usuario, uItem, loSigo)
                                        }
                                    ) {
                                        Text(if (loSigo) "Siguiendo" else "Seguir", color = colors.purple, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
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
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val ultimaLectura = seguimientos.firstOrNull()
                if (ultimaLectura != null) {
                    val novelaAsociada = novelasPublicadas.find { it.id == ultimaLectura.novelaId }
                    if (novelaAsociada != null) {
                        Surface(
                            color = colors.cardBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { onSeleccionarNovela(novelaAsociada) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("CONTINUAR LECTURA", fontSize = 10.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                                Text(novelaAsociada.titulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textDark)
                                Text("Capítulo ${ultimaLectura.capituloActual} • ${ultimaLectura.progresoPorcentaje}% leído", fontSize = 12.sp, color = colors.textMuted)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ultimaLectura.progresoPorcentaje / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = colors.purple
                                )
                            }
                        }
                    }
                }

                if (filtradas.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = colors.cardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                                        Text("Por ${novela.autor}", fontSize = 12.sp, color = colors.textMuted)
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
    onSeleccionarNovela: (Novela) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current
    var tabIndex by remember { mutableIntStateOf(0) }
    var mostrarOcultas by remember { mutableStateOf(true) }
    var offlineList by remember { mutableStateOf(OfflineStorage.obtenerNovelasOffline(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
    ) {
        Text("Mi Biblioteca", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = tabIndex, containerColor = colors.cardBg, contentColor = colors.purple) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Novelas (${seguimientos.size})") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Usuarios (${usuariosQueSigo.size})") })
            Tab(selected = tabIndex == 2, onClick = {
                offlineList = OfflineStorage.obtenerNovelasOffline(context)
                tabIndex = 2
            }, text = { Text("Offline (${offlineList.size})") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabIndex) {
            0 -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Mostrar novelas que oculté", fontSize = 13.sp, color = colors.textMuted)
                    Switch(checked = mostrarOcultas, onCheckedChange = { mostrarOcultas = it })
                }
                Spacer(modifier = Modifier.height(8.dp))

                val visibles = seguimientos.filter { mostrarOcultas || !it.oculta }
                if (visibles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes novelas seguidas visibles.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(visibles, key = { it.id }) { rel ->
                            val novela = novelas.find { it.id == rel.novelaId }
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (novela != null) onSeleccionarNovela(novela) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(rel.novelaTitulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                            Text("Autor: ${rel.seguidoAutorNombre}", fontSize = 12.sp, color = colors.purple)
                                            if (rel.oculta) {
                                                Text("🔒 Oculta para otros usuarios", fontSize = 11.sp, color = colors.textMuted)
                                            }
                                        }
                                        IconButton(onClick = { FirestoreService.alternarOcultarSeguimiento(rel.id, !rel.oculta) }) {
                                            Icon(
                                                imageVector = if (rel.oculta) Icons.Outlined.Lock else Icons.Outlined.Face,
                                                contentDescription = "Ocultar novela",
                                                tint = colors.purple
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
            1 -> {
                if (usuariosQueSigo.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aún no sigues a otros usuarios.", color = colors.textMuted)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(usuariosQueSigo, key = { it.id }) { segUser ->
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(segUser.seguidoNombre, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                    TextButton(onClick = {
                                        FirestoreService.seguirODejarDeSeguirUsuario(
                                            seguidor = UsuarioPerfil(id = segUser.seguidorId, usuario = segUser.seguidorNombre),
                                            objetivo = UsuarioPerfil(id = segUser.seguidoId, usuario = segUser.seguidoNombre),
                                            yaLoSigue = true
                                        )
                                    }) {
                                        Text("Dejar de seguir", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(offlineList, key = { it.id }) { novOff ->
                            Surface(
                                color = colors.cardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeleccionarNovela(novOff) }
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
    onVolver: () -> Unit,
    onLeerCapitulo: (Int) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current
    val relacion = seguimientos.find { it.novelaId == novela.id }
    val esMiPropiaNovela = novela.autorId == usuario.id
    var mensajeBanner by remember { mutableStateOf("") }
    var mostrarPerfilAutorYReporte by remember { mutableStateOf(false) }
    var motivoReporteUsuario by remember { mutableStateOf("") }

    val portadaBmp = remember(novela.portadaBase64) { ImageUtils.base64ToImageBitmap(novela.portadaBase64) }

    if (mostrarPerfilAutorYReporte) {
        AlertDialog(
            onDismissRequest = { mostrarPerfilAutorYReporte = false },
            containerColor = colors.cardBg,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Autor: ${novela.autor}", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.purple)
                    if (!esMiPropiaNovela) {
                        IconButton(onClick = {
                            FirestoreService.enviarReporte(
                                reportante = usuario,
                                tipoObjetivo = "USUARIO",
                                objetivoId = novela.autorId,
                                objetivoNombre = novela.autor,
                                motivo = motivoReporteUsuario.ifBlank { "Reporte desde perfil de autor" }
                            ) {
                                motivoReporteUsuario = ""
                                mostrarPerfilAutorYReporte = false
                                mensajeBanner = "Reporte enviado sobre el usuario ${novela.autor}."
                            }
                        }) {
                            IconoBocinaReporte(tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Obra: ${novela.titulo}", fontSize = 13.sp, color = colors.textDark)
                    if (!esMiPropiaNovela) {
                        Text(
                            "Si este usuario infringe las normas, escribe un motivo opcional y pulsa el ícono de bocina arriba a la derecha para reportarlo:",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                        OutlinedTextField(
                            value = motivoReporteUsuario,
                            onValueChange = { motivoReporteUsuario = it },
                            placeholder = { Text("Motivo del reporte (opcional)...", fontSize = 12.sp) },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarPerfilAutorYReporte = false }) {
                    Text("Cerrar", color = colors.purple)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(onClick = onVolver) {
            Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold)
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
                Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Por ${novela.autor}",
                        fontSize = 13.sp,
                        color = colors.purple,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { mostrarPerfilAutorYReporte = true }
                    )
                    if (!esMiPropiaNovela) {
                        IconButton(
                            onClick = { mostrarPerfilAutorYReporte = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            IconoBocinaReporte(
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
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
                            relacion.oculta -> "🔒 Oculta (Mostrar)"
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
    }
}

@Composable
fun LecturaScreen(
    usuario: UsuarioPerfil,
    novela: Novela,
    indiceCapituloInicial: Int = 0,
    seguimientos: List<SeguimientoRelacion>,
    modoOscuroApp: Boolean = false,
    onVolver: () -> Unit
) {
    val scrollState = rememberScrollState()
    var indiceCap by remember { mutableIntStateOf(indiceCapituloInicial.coerceIn(0, (novela.capitulos.size - 1).coerceAtLeast(0))) }

    var temaActual by remember(modoOscuroApp) {
        mutableStateOf(if (modoOscuroApp) ConfigTemas.Noche else ConfigTemas.Sepia)
    }
    var tamanoTexto by remember { mutableFloatStateOf(18f) }

    val esMiPropiaNovela = novela.autorId == usuario.id
    var vistaRegistrada by remember(novela.id, indiceCap) { mutableStateOf(false) }

    val capituloActual = novela.capitulos.getOrNull(indiceCap)
    var comentarios by remember { mutableStateOf<List<ComentarioItem>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var comentarioAReportar by remember { mutableStateOf<ComentarioItem?>(null) }
    var motivoReporteComentario by remember { mutableStateOf("") }
    var avisoReporte by remember { mutableStateOf("") }

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
        if (porcentajeLectura >= 100 && !vistaRegistrada && capituloActual != null && !esMiPropiaNovela) {
            vistaRegistrada = true
            FirestoreService.incrementarVistaNovela(novela, usuario.id)
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
                        placeholder = { Text("Motivo del reporte (ej. insultos, spam)...") },
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

    Column(modifier = Modifier.fillMaxSize().background(temaActual.fondo)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
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
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
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
                            onClick = { FirestoreService.registrarReaccionCapitulo(novela, capituloActual.id, "CORAZON") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Corazón", tint = Color(0xFFD81B60), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${capituloActual.corazones}", color = Color(0xFFD81B60), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { FirestoreService.registrarReaccionCapitulo(novela, capituloActual.id, "LIKE") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.ThumbUp, contentDescription = "Like", tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${capituloActual.meGusta}", color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { FirestoreService.registrarReaccionCapitulo(novela, capituloActual.id, "DISLIKE") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👎 ${capituloActual.noMeGusta}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (avisoReporte.isNotEmpty()) {
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
                    Card(
                        colors = CardDefaults.cardColors(containerColor = temaActual.texto.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(com.usuarioNombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = temaActual.acento)
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
            }
        }
    }
}