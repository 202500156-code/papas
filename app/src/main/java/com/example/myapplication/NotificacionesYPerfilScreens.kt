package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

@Composable
fun NotificacionesScreen(
    notificaciones: List<NotificacionItem>,
    onVolver: () -> Unit
) {
    val colors = LocalReadwoodColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onVolver) {
                Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold)
            }
            Text(
                "Notificaciones",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones activas.", color = colors.textMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notificaciones, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.mensaje,
                                    color = colors.textDark,
                                    fontSize = 14.sp,
                                    fontWeight = if (!item.leida) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(item.fecha, color = colors.textMuted, fontSize = 11.sp)
                            }
                            if (!item.leida) {
                                TextButton(onClick = { FirestoreService.marcarNotificacionLeida(item.id) }) {
                                    Text("Leída", color = colors.purple, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { FirestoreService.borradoLogicoNotificacion(item.id) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Ocultar notificación",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilScreen(
    usuario: UsuarioPerfil,
    misNovelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    usuariosQueSigo: List<UsuarioSeguimiento>,
    modoOscuro: Boolean,
    onToggleModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    var novelaGraficasId by remember { mutableStateOf<String?>(null) }

    var fotoBase64 by remember(usuario.fotoBase64) { mutableStateOf(usuario.fotoBase64) }
    var mostrarConsentimientoCambioFoto by remember { mutableStateOf(false) }

    var instagram by remember(usuario.redInstagram) { mutableStateOf(usuario.redInstagram) }
    var twitter by remember(usuario.redTwitter) { mutableStateOf(usuario.redTwitter) }
    var consintioRedes by remember(usuario.consintioRedes) { mutableStateOf(usuario.consintioRedes) }
    var mostrarDialogoRedes by remember { mutableStateOf(false) }

    var ocultarUsuariosSeguidos by remember(usuario.ocultarSeguidos) { mutableStateOf(usuario.ocultarSeguidos) }
    var ocultarNovelasVistas by remember(usuario.ocultarNovelasVistas) { mutableStateOf(usuario.ocultarNovelasVistas) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }

    fun actualizarSesionLocal(
        nuevaFoto: String = fotoBase64,
        nuevoIg: String = instagram,
        nuevoTw: String = twitter,
        nuevoConsentRedes: Boolean = consintioRedes,
        nuevoOcultarUsers: Boolean = ocultarUsuariosSeguidos,
        nuevoOcultarNovelas: Boolean = ocultarNovelasVistas
    ) {
        val actualizado = usuario.copy(
            fotoBase64 = nuevaFoto,
            consintioFoto = nuevaFoto.isNotEmpty(),
            redInstagram = nuevoIg.trim(),
            redTwitter = nuevoTw.trim(),
            consintioRedes = nuevoConsentRedes,
            ocultarSeguidos = nuevoOcultarUsers,
            ocultarNovelasVistas = nuevoOcultarNovelas
        )
        SessionManager.guardarSesion(context, actualizado)
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val nuevaFoto = ImageUtils.uriToCompressedBase64(context, uri)
            if (nuevaFoto.isNotEmpty()) {
                fotoBase64 = nuevaFoto
                FirestoreService.actualizarFotoPerfil(usuario.id, nuevaFoto, true)
                actualizarSesionLocal(nuevaFoto = nuevaFoto)
            }
        }
    }

    if (mostrarConsentimientoCambioFoto) {
        AlertDialog(
            onDismissRequest = { mostrarConsentimientoCambioFoto = false },
            containerColor = colors.cardBg,
            title = { Text("Cambiar Foto de Perfil", fontWeight = FontWeight.Bold, color = colors.purple) },
            text = {
                Text(
                    "¿Autorizas el acceso a tu galería para actualizar tu foto de perfil en Readwood?",
                    color = colors.textDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConsentimientoCambioFoto = false
                        galeriaLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) { Text("Autorizar y abrir galería", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConsentimientoCambioFoto = false }) {
                    Text("Cancelar", color = colors.textMuted)
                }
            }
        )
    }

    if (mostrarDialogoRedes) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRedes = false },
            containerColor = colors.cardBg,
            title = { Text("Adjuntar Redes Sociales", fontWeight = FontWeight.Bold, color = colors.purple) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tu correo nunca se muestra. Si aceptas, tus enlaces sociales serán visibles en tu perfil de autor:",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    OutlinedTextField(
                        value = instagram,
                        onValueChange = { instagram = it },
                        label = { Text("Instagram (@usuario)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = twitter,
                        onValueChange = { twitter = it },
                        label = { Text("X / Twitter (@usuario)") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = consintioRedes, onCheckedChange = { consintioRedes = it })
                        Text("Doy mi consentimiento para mostrar mis redes", fontSize = 12.sp, color = colors.textDark)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FirestoreService.actualizarRedesUsuario(usuario.id, instagram, twitter, consintioRedes)
                        actualizarSesionLocal(
                            nuevoIg = instagram,
                            nuevoTw = twitter,
                            nuevoConsentRedes = consintioRedes
                        )
                        mostrarDialogoRedes = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) { Text("Guardar Redes", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRedes = false }) { Text("Cerrar", color = colors.textMuted) }
            }
        )
    }

    if (mostrarAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarAcercaDe = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    "Acerca de Readwood",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.purple
                )
            },
            text = {
                Text(
                    "Readwood es una plataforma editorial independiente diseñada para lectores y escritores.\n\n• Sincronización multi-dispositivo encriptada en Firebase.\n• Control total de privacidad sobre borradores, usuarios seguidos y novelas leídas.\n• Lectura sin conexión almacenada exclusivamente dentro de la app.",
                    fontSize = 13.sp,
                    color = colors.textDark,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarAcercaDe = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        )
    }

    if (mostrarConfirmacionSalir) {
        ConfirmDialog(
            title = "¿Deseas cerrar tu sesión en Readwood?",
            onConfirm = {
                mostrarConfirmacionSalir = false
                onCerrarSesion()
            },
            onDismiss = { mostrarConfirmacionSalir = false }
        )
    }

    val novelaSeleccionadaParaGraficas = misNovelas.find { it.id == novelaGraficasId }
    if (novelaSeleccionadaParaGraficas != null) {
        PantallaGraficasNovela(
            novela = novelaSeleccionadaParaGraficas,
            onVolver = { novelaGraficasId = null }
        )
        return
    }

    val bitmapPerfil = remember(fotoBase64) { ImageUtils.base64ToImageBitmap(fotoBase64) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Mi Perfil",
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )

        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.bg)
                        .border(1.5.dp, colors.purple, CircleShape)
                        .clickable { mostrarConsentimientoCambioFoto = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapPerfil != null) {
                        Image(
                            bitmap = bitmapPerfil,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = colors.purple,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        usuario.usuario,
                        fontFamily = FontFamily.Serif,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.purple
                    )
                    Text(
                        text = "${usuariosQueSigo.size} usuarios seguidos • ${seguimientos.size} novelas",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    if (consintioRedes && (instagram.isNotBlank() || twitter.isNotBlank())) {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (instagram.isNotBlank()) Text("IG: $instagram", fontSize = 12.sp, color = colors.textDark)
                        if (twitter.isNotBlank()) Text("X: $twitter", fontSize = 12.sp, color = colors.textDark)
                    }
                    TextButton(
                        onClick = { mostrarDialogoRedes = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "+ Adjuntar o editar redes sociales",
                            fontSize = 12.sp,
                            color = colors.purple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { pestanaSeleccionada = 0 }
            ) {
                Text(
                    text = "Progreso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pestanaSeleccionada == 0) colors.textDark else colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(if (pestanaSeleccionada == 0) colors.purple else colors.border)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { pestanaSeleccionada = 1 }
            ) {
                Text(
                    text = "Ajustes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pestanaSeleccionada == 1) colors.textDark else colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(if (pestanaSeleccionada == 1) colors.purple else colors.border)
                )
            }
        }

        if (pestanaSeleccionada == 0) {
            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Progreso de Mis Novelas", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 16.sp)
                    Text("Toca una novela para ver sus gráficas detalladas", fontSize = 12.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    if (misNovelas.isEmpty()) {
                        Text("Aún no tienes novelas creadas.", fontSize = 13.sp, color = colors.textMuted)
                    } else {
                        misNovelas.forEach { nov ->
                            val capsPublicados = nov.capitulos.count { !it.esBorrador }
                            val totalCaps = nov.capitulos.size.coerceAtLeast(1)
                            val ratio = capsPublicados.toFloat() / totalCaps.toFloat()
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.bg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { novelaGraficasId = nov.id }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(nov.titulo, fontWeight = FontWeight.Bold, color = colors.textDark, fontSize = 15.sp)
                                        Text("Ver gráficas →", fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "$capsPublicados/$totalCaps cap. • ♥ ${nov.corazones} • 👍 ${nov.meGusta} • 👎 ${nov.noMeGusta} • 👁 ${nov.vistas} • 💬 ${nov.totalComentarios}",
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                        color = colors.purple
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ajustes y Privacidad", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 16.sp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Modo Oscuro Editorial", color = colors.textDark, fontSize = 14.sp)
                        Switch(checked = modoOscuro, onCheckedChange = onToggleModoOscuro)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ocultar a qué usuarios sigo (${usuariosQueSigo.size})", color = colors.textDark, fontSize = 14.sp)
                        Switch(checked = ocultarUsuariosSeguidos, onCheckedChange = {
                            ocultarUsuariosSeguidos = it
                            FirestoreService.actualizarPrivacidadSeguidos(usuario.id, it, ocultarNovelasVistas)
                            actualizarSesionLocal(nuevoOcultarUsers = it)
                        })
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ocultar las novelas que sigo/veo (${seguimientos.size})", color = colors.textDark, fontSize = 14.sp)
                        Switch(checked = ocultarNovelasVistas, onCheckedChange = {
                            ocultarNovelasVistas = it
                            FirestoreService.actualizarPrivacidadSeguidos(usuario.id, ocultarUsuariosSeguidos, it)
                            actualizarSesionLocal(nuevoOcultarNovelas = it)
                        })
                    }

                    OutlinedButton(onClick = { mostrarConsentimientoCambioFoto = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cambiar foto de perfil desde galería", color = colors.purple)
                    }

                    OutlinedButton(onClick = { mostrarAcercaDe = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Acerca de nosotros", color = colors.purple)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { mostrarConfirmacionSalir = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PantallaGraficasNovela(
    novela: Novela,
    onVolver: () -> Unit
) {
    val colors = LocalReadwoodColors.current

    val datosCorazones = if (novela.capitulos.isNotEmpty()) {
        novela.capitulos.map { it.corazones.toFloat() }
    } else listOf(novela.corazones.toFloat())

    val datosLikes = if (novela.capitulos.isNotEmpty()) {
        novela.capitulos.map { it.meGusta.toFloat() }
    } else listOf(novela.meGusta.toFloat())

    val datosDislikes = if (novela.capitulos.isNotEmpty()) {
        novela.capitulos.map { it.noMeGusta.toFloat() }
    } else listOf(novela.noMeGusta.toFloat())

    val datosComentarios = if (novela.capitulos.isNotEmpty()) {
        novela.capitulos.map { it.comentariosCount.toFloat() }
    } else listOf(novela.totalComentarios.toFloat())

    val datosVistas = if (novela.capitulos.isNotEmpty()) {
        novela.capitulos.mapIndexed { idx, _ ->
            ((novela.vistas.toFloat() / novela.capitulos.size.coerceAtLeast(1)) * (idx + 1) / novela.capitulos.size.coerceAtLeast(1)).coerceAtLeast(0f)
        }
    } else listOf(novela.vistas.toFloat())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver) {
                Text("← Volver a Progreso", color = colors.purple, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Estadísticas: ${novela.titulo}",
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )
        Text(
            text = "Por ${novela.autor} • ${novela.capitulos.size} capítulo(s)",
            fontSize = 13.sp,
            color = colors.purple
        )

        TarjetaGraficaSimple(
            titulo = "Corazones (♥ ${novela.corazones} en total)",
            subtitulo = "Distribución por capítulo",
            valores = datosCorazones,
            colorLinea = Color(0xFFD81B60)
        )

        TarjetaGraficaDobleLikesDislikes(
            totalLikes = novela.meGusta,
            totalDislikes = novela.noMeGusta,
            valoresLikes = datosLikes,
            valoresDislikes = datosDislikes
        )

        TarjetaGraficaSimple(
            titulo = "Comentarios (💬 ${novela.totalComentarios} en total)",
            subtitulo = "Actividad por capítulo",
            valores = datosComentarios,
            colorLinea = Color(0xFF5E2D40)
        )

        TarjetaGraficaSimple(
            titulo = "Vistas Completas (👁 ${novela.vistas} en total)",
            subtitulo = "Lecturas terminadas al 100%",
            valores = datosVistas,
            colorLinea = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun TarjetaGraficaSimple(
    titulo: String,
    subtitulo: String,
    valores: List<Float>,
    colorLinea: Color
) {
    val colors = LocalReadwoodColors.current
    Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textDark)
            Text(subtitulo, fontSize = 11.sp, color = colors.textMuted)
            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height
                val maxVal = (valores.maxOrNull() ?: 1f).coerceAtLeast(1f)

                drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0f, h), Offset(w, h), strokeWidth = 2f)
                drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)

                val puntos = if (valores.size == 1) {
                    val y = h - (valores[0] / maxVal) * (h * 0.8f) - 10f
                    listOf(Offset(w * 0.15f, y), Offset(w * 0.85f, y))
                } else {
                    valores.mapIndexed { i, v ->
                        val x = (w / (valores.size - 1)) * i
                        val y = h - (v / maxVal) * (h * 0.8f) - 10f
                        Offset(x, y)
                    }
                }

                val path = Path().apply {
                    moveTo(puntos.first().x, puntos.first().y)
                    for (i in 1 until puntos.size) {
                        lineTo(puntos[i].x, puntos[i].y)
                    }
                }
                drawPath(path, color = colorLinea, style = Stroke(width = 6f, cap = StrokeCap.Round))
                puntos.forEach { pt ->
                    drawCircle(color = colorLinea, radius = 8f, center = pt)
                }
            }
        }
    }
}

@Composable
private fun TarjetaGraficaDobleLikesDislikes(
    totalLikes: Int,
    totalDislikes: Int,
    valoresLikes: List<Float>,
    valoresDislikes: List<Float>
) {
    val colors = LocalReadwoodColors.current
    val azulLikes = Color(0xFF1976D2)
    val rojoDislikes = Color(0xFFD32F2F)

    Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Likes vs Dislikes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textDark)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 4.dp)) {
                Text("● Likes: $totalLikes (Línea Azul)", fontSize = 12.sp, color = azulLikes, fontWeight = FontWeight.Bold)
                Text("● Dislikes: $totalDislikes (Línea Roja)", fontSize = 12.sp, color = rojoDislikes, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            ) {
                val w = size.width
                val h = size.height
                val maxCombinado = (valoresLikes + valoresDislikes).maxOrNull()?.coerceAtLeast(1f) ?: 1f

                drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0f, h), Offset(w, h), strokeWidth = 2f)
                drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)

                fun construirPuntos(lista: List<Float>): List<Offset> {
                    return if (lista.size == 1) {
                        val y = h - (lista[0] / maxCombinado) * (h * 0.8f) - 10f
                        listOf(Offset(w * 0.15f, y), Offset(w * 0.85f, y))
                    } else {
                        lista.mapIndexed { i, v ->
                            val x = (w / (lista.size - 1)) * i
                            val y = h - (v / maxCombinado) * (h * 0.8f) - 10f
                            Offset(x, y)
                        }
                    }
                }

                val ptsLikes = construirPuntos(valoresLikes)
                val ptsDislikes = construirPuntos(valoresDislikes)

                val pathLikes = Path().apply {
                    moveTo(ptsLikes.first().x, ptsLikes.first().y)
                    for (i in 1 until ptsLikes.size) lineTo(ptsLikes[i].x, ptsLikes[i].y)
                }
                val pathDislikes = Path().apply {
                    moveTo(ptsDislikes.first().x, ptsDislikes.first().y)
                    for (i in 1 until ptsDislikes.size) lineTo(ptsDislikes[i].x, ptsDislikes[i].y)
                }

                drawPath(pathLikes, color = azulLikes, style = Stroke(width = 6f, cap = StrokeCap.Round))
                ptsLikes.forEach { drawCircle(color = azulLikes, radius = 8f, center = it) }

                drawPath(pathDislikes, color = rojoDislikes, style = Stroke(width = 6f, cap = StrokeCap.Round))
                ptsDislikes.forEach { drawCircle(color = rojoDislikes, radius = 8f, center = it) }
            }
        }
    }
}