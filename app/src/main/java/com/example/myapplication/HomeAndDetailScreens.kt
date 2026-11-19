package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: UsuarioPerfil,
    novelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    notificacionesSinLeer: Int,
    onAbrirNotificaciones: () -> Unit,
    onSeleccionarNovela: (Novela) -> Unit
) {
    val colors = LocalReadwoodColors.current
    var textoBusqueda by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("Todos") }
    val generos = listOf("Todos", "Fantasía", "Romance", "Misterio", "Terror", "Ciencia Ficción")

    val novelasPublicadas = novelas.filter { it.esPublicada }
    val filtradas = novelasPublicadas.filter { n ->
        (n.titulo.contains(textoBusqueda, true) || n.autor.contains(textoBusqueda, true)) &&
                (generoSeleccionado == "Todos" || n.categoria.equals(generoSeleccionado, true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HOLA, ${usuario.usuario.uppercase()}",
                            fontSize = 11.sp,
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
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar novelas o autores...", color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(generos) { gen ->
                    val sel = generoSeleccionado == gen
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (sel) colors.purple else colors.cardBg,
                        border = if (sel) null else BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable { generoSeleccionado = gen }
                    ) {
                        Text(
                            text = gen,
                            fontSize = 12.sp,
                            color = if (sel) Color.White else colors.textDark,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val ultimaLectura = seguimientos.firstOrNull()
            if (ultimaLectura != null) {
                val novelaAsociada = novelasPublicadas.find { it.id == ultimaLectura.novelaId }
                if (novelaAsociada != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onSeleccionarNovela(novelaAsociada) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("VUELVE A TU LECTURA", fontSize = 11.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                            Text(novelaAsociada.titulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = colors.textDark)
                            Text("Capítulo ${ultimaLectura.capituloActual} • ${ultimaLectura.progresoPorcentaje}% leído", fontSize = 12.sp, color = colors.textMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { ultimaLectura.progresoPorcentaje / 100f },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                                color = colors.purple
                            )
                        }
                    }
                }
            }

            if (filtradas.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    shape = RoundedCornerShape(12.dp),
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeleccionarNovela(novela) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(92.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(parseHexColorSafe(novela.portadaColorHex, colors.purple)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (portadaBmp != null) {
                                        Image(bitmap = portadaBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Text(
                                            text = novela.titulo.take(12),
                                            color = Color.White,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(novela.categoria.uppercase(), fontSize = 10.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                                    Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                                    Text("Por ${novela.autor}", fontSize = 12.sp, color = colors.textMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(novela.sinopsis, fontSize = 12.sp, color = colors.textDark, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "♥ ${novela.meGusta}   •   👁 ${novela.vistas}   •   💬 ${novela.totalComentarios}",
                                        fontSize = 11.sp,
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
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Seguidas (${seguimientos.size})") })
            Tab(selected = tabIndex == 1, onClick = {
                offlineList = OfflineStorage.obtenerNovelasOffline(context)
                tabIndex = 1
            }, text = { Text("En la App (${offlineList.size})") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tabIndex == 0) {
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (novela != null) onSeleccionarNovela(novela) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(rel.novelaTitulo, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                                        Text("Autor seguido: ${rel.seguidoAutorNombre}", fontSize = 12.sp, color = colors.purple)
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
        } else {
            if (offlineList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay novelas guardadas para lectura interna.", color = colors.textMuted)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(offlineList, key = { it.id }) { novOff ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
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
    var mensajeBanner by remember { mutableStateOf("") }
    val portadaBmp = remember(novela.portadaBase64) { ImageUtils.base64ToImageBitmap(novela.portadaBase64) }

    LaunchedEffect(novela.id) {
        FirestoreService.incrementarVistaNovela(novela)
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
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(138.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(parseHexColorSafe(novela.portadaColorHex, colors.purple)),
                contentAlignment = Alignment.Center
            ) {
                if (portadaBmp != null) {
                    Image(bitmap = portadaBmp, contentDescription = "Portada", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(novela.titulo, color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 3)
                        if (novela.portadaSubtitulo.isNotBlank()) {
                            Text(novela.portadaSubtitulo, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(novela.categoria.uppercase(), fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                Text(novela.titulo, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                Text("Por ${novela.autor}", fontSize = 13.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Text("♥ ${novela.meGusta} Likes  •  👁 ${novela.vistas} Vistas  •  💬 ${novela.totalComentarios}", fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(novela.sinopsis, fontSize = 14.sp, color = colors.textDark)

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

        if (mensajeBanner.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(mensajeBanner, fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Capítulos (${novela.capitulos.size})", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
        Spacer(modifier = Modifier.height(8.dp))

        novela.capitulos.forEachIndexed { index, cap ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
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

@Composable
fun LecturaScreen(
    usuario: UsuarioPerfil,
    novela: Novela,
    indiceCapituloInicial: Int = 0,
    seguimientos: List<SeguimientoRelacion>,
    onVolver: () -> Unit
) {
    val scrollState = rememberScrollState()
    var indiceCap by remember { mutableIntStateOf(indiceCapituloInicial.coerceIn(0, (novela.capitulos.size - 1).coerceAtLeast(0))) }
    var temaActual by remember { mutableStateOf(ConfigTemas.Sepia) }
    var tamanoTexto by remember { mutableFloatStateOf(18f) }

    val capituloActual = novela.capitulos.getOrNull(indiceCap)
    var comentarios by remember { mutableStateOf<List<ComentarioItem>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }

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
        val rel = seguimientos.find { it.novelaId == novela.id }
        if (rel != null && porcentajeLectura > 0) {
            FirestoreService.actualizarProgresoLectura(rel.id, indiceCap + 1, porcentajeLectura)
        }
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
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { FirestoreService.registrarVotoNovela(novela, true) }) {
                        Text("♥ Me gusta (${novela.meGusta})", color = temaActual.acento)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (indiceCap > 0) {
                            TextButton(onClick = { indiceCap-- }) { Text("← Anterior", color = temaActual.texto) }
                        }
                        if (indiceCap < novela.capitulos.lastIndex) {
                            TextButton(onClick = { indiceCap++ }) { Text("Siguiente →", color = temaActual.acento) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Comentarios en tiempo real (${comentarios.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = temaActual.texto
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        placeholder = { Text("Escribe un comentario sobre este capítulo...") },
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
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(com.usuarioNombre, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = temaActual.acento)
                            Text(com.texto, fontSize = 14.sp, color = temaActual.texto)
                        }
                    }
                }
            }
        }
    }
}