package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

@Composable
fun NotificacionesScreen(
    notificaciones: List<NotificacionItem>,
    onVolver: () -> Unit
) {
    BackHandler { onVolver() }
    val colors = LocalReadwoodColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onVolver,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 30.dp)
            ) {
                Text(
                    text = "← Volver",
                    color = colors.purple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notificaciones",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textDark
            )
        }

        if (notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones activas.", color = colors.textMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                items(notificaciones, key = { it.id }) { notif ->
                    Surface(
                        color = if (notif.leida) colors.cardBg else colors.purple.copy(alpha = 0.09f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { FirestoreService.marcarNotificacionLeida(notif.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.mensaje,
                                    fontWeight = if (notif.leida) FontWeight.Normal else FontWeight.Bold,
                                    color = colors.textDark,
                                    fontSize = 14.sp
                                )
                                Text(notif.fecha, fontSize = 11.sp, color = colors.textMuted)
                            }
                            IconButton(onClick = { FirestoreService.borradoLogicoNotificacion(notif.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Ocultar", tint = MaterialTheme.colorScheme.error)
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
    usuariosQueSigo: List<UsuarioSeguimiento>,
    modoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onEditarNovela: (Novela) -> Unit,
    onAbrirPerfilAutor: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var novelaEstadisticasId by remember { mutableStateOf<String?>(null) }
    val novelaEstadisticas = misNovelas.find { it.id == novelaEstadisticasId }

    if (novelaEstadisticas != null) {
        EstadisticasDetalleNovelaScreen(
            novela = novelaEstadisticas,
            onVolver = { novelaEstadisticasId = null }
        )
        return
    }

    var mostrarAjustes by remember { mutableStateOf(false) }
    var mostrarSelectorBanner by remember { mutableStateOf(false) }
    var nuevaRedInput by remember { mutableStateOf("") }
    var listaEnlacesTemp by remember(usuario.enlacesRedes) { mutableStateOf(usuario.enlacesRedes) }
    var mostrarCorreoTemp by remember(usuario.mostrarCorreoPublico) { mutableStateOf(usuario.mostrarCorreoPublico) }
    var notifsSilenciadas by remember { mutableStateOf(NotificationHelper.estaSilenciado(context)) }

    val prefsPermisos = remember(usuario.id) {
        context.getSharedPreferences("readwood_permisos_${usuario.id}", Context.MODE_PRIVATE)
    }

    var yaAutorizoFoto by remember(usuario.consintioFoto) {
        mutableStateOf(usuario.consintioFoto || prefsPermisos.getBoolean("autorizo_foto", false))
    }
    var yaAutorizoBanner by remember {
        mutableStateOf(prefsPermisos.getBoolean("autorizo_banner", false))
    }

    var uriFotoPendiente by remember { mutableStateOf<Uri?>(null) }
    var uriBannerPendiente by remember { mutableStateOf<Uri?>(null) }
    var mostrarConfirmacionFoto by remember { mutableStateOf(false) }
    var mostrarConfirmacionBanner by remember { mutableStateOf(false) }
    var mensajePerfil by remember { mutableStateOf("") }

    val selectorGaleriaFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            uriFotoPendiente = uri
            mostrarConfirmacionFoto = !yaAutorizoFoto
        }
    }

    val selectorGaleriaBanner = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            uriBannerPendiente = uri
            mostrarConfirmacionBanner = !yaAutorizoBanner
        }
    }

    if (uriFotoPendiente != null && mostrarConfirmacionFoto) {
        AlertDialog(
            onDismissRequest = {
                uriFotoPendiente = null
                mostrarConfirmacionFoto = false
            },
            title = { Text("Permiso de Foto de Perfil") },
            text = { Text("¿Autorizas subir imágenes como tu avatar público en Readwood?") },
            confirmButton = {
                Button(onClick = {
                    yaAutorizoFoto = true
                    prefsPermisos.edit().putBoolean("autorizo_foto", true).apply()
                    mostrarConfirmacionFoto = false
                }) { Text("Sí, autorizo") }
            },
            dismissButton = {
                TextButton(onClick = {
                    uriFotoPendiente = null
                    mostrarConfirmacionFoto = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (uriFotoPendiente != null && !mostrarConfirmacionFoto) {
        ImageCropperDialog(
            imageUri = uriFotoPendiente!!,
            esCircular = true,
            onDismiss = { uriFotoPendiente = null },
            onConfirmarRecorte = { b64 ->
                if (b64.isNotEmpty()) {
                    FirestoreService.actualizarFotoPerfil(usuario.id, b64, true)
                    mensajePerfil = "Foto de perfil actualizada."
                }
                uriFotoPendiente = null
            }
        )
    }

    if (uriBannerPendiente != null && mostrarConfirmacionBanner) {
        AlertDialog(
            onDismissRequest = {
                uriBannerPendiente = null
                mostrarConfirmacionBanner = false
            },
            title = { Text("Permiso de Imagen de Fondo") },
            text = { Text("¿Autorizas usar imágenes de tu galería como fondo en tu perfil?") },
            confirmButton = {
                Button(onClick = {
                    yaAutorizoBanner = true
                    prefsPermisos.edit().putBoolean("autorizo_banner", true).apply()
                    mostrarConfirmacionBanner = false
                }) { Text("Sí, autorizo") }
            },
            dismissButton = {
                TextButton(onClick = {
                    uriBannerPendiente = null
                    mostrarConfirmacionBanner = false
                }) { Text("Cancelar") }
            }
        )
    }

    if (uriBannerPendiente != null && !mostrarConfirmacionBanner) {
        ImageCropperDialog(
            imageUri = uriBannerPendiente!!,
            esCircular = false,
            onDismiss = { uriBannerPendiente = null },
            onConfirmarRecorte = { b64 ->
                if (b64.isNotEmpty()) {
                    FirestoreService.actualizarBannerPerfil(usuario.id, b64, usuario.bannerColorHex)
                    mensajePerfil = "Imagen de fondo actualizada."
                }
                uriBannerPendiente = null
            }
        )
    }

    if (mostrarSelectorBanner) {
        val coloresBanner = listOf("#5E2D40", "#1E3A5F", "#2E4A3E", "#4A2C5E", "#7A3B2E", "#2C2C2C")
        AlertDialog(
            onDismissRequest = { mostrarSelectorBanner = false },
            containerColor = colors.cardBg,
            title = { Text("Personalizar Fondo del Perfil", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Elige un color de fondo:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        coloresBanner.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColorSafe(hex, colors.purple))
                                    .border(2.dp, if (usuario.bannerColorHex == hex && usuario.bannerBase64.isEmpty()) Color.White else Color.Transparent, CircleShape)
                                    .clickable {
                                        FirestoreService.actualizarBannerPerfil(usuario.id, "", hex)
                                        mostrarSelectorBanner = false
                                    }
                            )
                        }
                    }

                    HorizontalDivider(color = colors.border)

                    Button(
                        onClick = {
                            mostrarSelectorBanner = false
                            selectorGaleriaBanner.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar y ajustar imagen", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorBanner = false }) { Text("Cerrar", color = colors.purple) }
            }
        )
    }

    val fotoBitmap = remember(usuario.fotoBase64) { ImageUtils.base64ToImageBitmap(usuario.fotoBase64) }
    val bannerBitmap = remember(usuario.bannerBase64) { ImageUtils.base64ToImageBitmap(usuario.bannerBase64) }
    val bannerColor = remember(usuario.bannerColorHex) { parseHexColorSafe(usuario.bannerColorHex, colors.purple) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Banner de fondo más grande (215.dp / 175.dp) + Avatar
        Box(modifier = Modifier.fillMaxWidth().height(215.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(bannerColor)
                    .clickable { mostrarSelectorBanner = true }
            ) {
                if (bannerBitmap != null) {
                    Image(bitmap = bannerBitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                ) {
                    Text(
                        text = "🎨 Cambiar fondo",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(colors.cardBg)
                    .border(2.5.dp, colors.bg, CircleShape)
                    .clickable { selectorGaleriaFoto.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoBitmap != null) {
                    Image(bitmap = fotoBitmap, contentDescription = "Foto perfil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple, modifier = Modifier.size(56.dp))
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(usuario.usuario, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
                    Text(
                        text = "${usuariosQueSigo.size} usuarios seguidos • ${misNovelas.size} novelas",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
                IconButton(onClick = { mostrarAjustes = !mostrarAjustes }) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Ajustes", tint = colors.purple)
                }
            }

            if (mensajePerfil.isNotEmpty()) {
                Text(mensajePerfil, fontSize = 12.sp, color = colors.purple, fontWeight = FontWeight.SemiBold)
            }

            if (mostrarAjustes) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = colors.cardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ajustes de Cuenta", fontWeight = FontWeight.Bold, color = colors.purple)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Modo Oscuro de la Aplicación", fontSize = 14.sp, color = colors.textDark)
                            Switch(checked = modoOscuro, onCheckedChange = onCambiarModoOscuro)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Silenciar todas las notificaciones", fontSize = 14.sp, color = colors.textDark)
                                Text("Desactiva alertas en barra superior y pantalla de bloqueo.", fontSize = 11.sp, color = colors.textMuted)
                            }
                            Switch(
                                checked = notifsSilenciadas,
                                onCheckedChange = {
                                    notifsSilenciadas = it
                                    NotificationHelper.setSilenciado(context, it)
                                }
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mostrar mi correo en mi perfil", fontSize = 14.sp, color = colors.textDark)
                                Text("Si está desactivado, tu correo será privado.", fontSize = 11.sp, color = colors.textMuted)
                            }
                            Switch(checked = mostrarCorreoTemp, onCheckedChange = { mostrarCorreoTemp = it })
                        }

                        HorizontalDivider(color = colors.border)

                        Text("Redes Sociales", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = nuevaRedInput,
                                onValueChange = { nuevaRedInput = it },
                                placeholder = { Text("Ej. https://miweb.com o @usuario", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (nuevaRedInput.isNotBlank()) {
                                        listaEnlacesTemp = listaEnlacesTemp + nuevaRedInput.trim()
                                        nuevaRedInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                            ) {
                                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        listaEnlacesTemp.forEachIndexed { idx, link ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔗 $link", fontSize = 12.sp, color = colors.purple, modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { listaEnlacesTemp = listaEnlacesTemp.filterIndexed { i, _ -> i != idx } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Quitar enlace", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Button(
                            onClick = {
                                FirestoreService.actualizarEnlacesYCorreoPublico(usuario.id, listaEnlacesTemp, mostrarCorreoTemp)
                                mensajePerfil = "Cambios guardados."
                                mostrarAjustes = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Cambios", color = Color.White)
                        }

                        HorizontalDivider(color = colors.border)

                        OutlinedButton(
                            onClick = onCerrarSesion,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = colors.cardBg,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Progreso de Mis Novelas",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.purple
                    )
                    Text(
                        text = "Toca una novela para ver sus gráficas detalladas",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )

                    if (misNovelas.isEmpty()) {
                        Text(
                            text = "Aún no has creado novelas. Ve a Escribir para comenzar.",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    } else {
                        misNovelas.forEach { nov ->
                            val capsPublicados = nov.capitulos.count { !it.esBorrador }
                            val totalCaps = nov.capitulos.size.coerceAtLeast(1)

                            Surface(
                                color = colors.bg.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { novelaEstadisticasId = nov.id }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = nov.titulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = colors.textDark,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "Ver gráficas →",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.purple
                                        )
                                    }

                                    Text(
                                        text = "$capsPublicados/$totalCaps cap. • ♥ ${nov.corazones} • 👍 ${nov.meGusta} • 👎 ${nov.noMeGusta} • 👁 ${nov.vistas} • 💬 ${nov.totalComentarios}",
                                        fontSize = 12.sp,
                                        color = colors.textMuted
                                    )

                                    HorizontalDivider(
                                        thickness = 2.5.dp,
                                        color = colors.purple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = colors.border)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Redes Sociales", fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
            Spacer(modifier = Modifier.height(6.dp))

            if (usuario.enlacesRedes.isEmpty()) {
                Text("Aún no hay redes", fontSize = 13.sp, color = colors.textMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    usuario.enlacesRedes.forEach { enlace ->
                        Text(
                            text = "🔗 $enlace",
                            fontSize = 13.sp,
                            color = colors.purple,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val url = if (enlace.startsWith("http://") || enlace.startsWith("https://")) enlace else "https://$enlace"
                                try { uriHandler.openUri(url) } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticasDetalleNovelaScreen(
    novela: Novela,
    onVolver: () -> Unit
) {
    BackHandler { onVolver() }
    val colors = LocalReadwoodColors.current

    val capitulos = novela.capitulos.ifEmpty {
        listOf(Capitulo(numero = 1, titulo = "Cap. 1"))
    }
    val etiquetasCaps = capitulos.map { "Cap. ${it.numero}" }

    val datosCorazones = capitulos.map { it.corazones.toFloat() }
    val datosLikes = capitulos.map { it.meGusta.toFloat() }
    val datosDislikes = capitulos.map { it.noMeGusta.toFloat() }
    val datosComentarios = capitulos.map { it.comentariosCount.toFloat() }
    val datosVistas = capitulos.map { it.vistas.toFloat() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(
            onClick = onVolver,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text("← Volver a Perfil", color = colors.purple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Column {
            Text(
                text = "Estadísticas: ${novela.titulo}",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colors.textDark
            )
            Text(
                text = "Por ${novela.autor} • ${novela.capitulos.size} capítulo(s) • Actualización en vivo",
                fontSize = 12.sp,
                color = colors.purple
            )
        }

        TarjetaGraficaLineaSimple(
            titulo = "Corazones (♥ ${novela.corazones} en total)",
            valores = datosCorazones,
            colorLinea = Color(0xFFE91E63),
            pieDetalle = capitulos.joinToString(" • ") { "Cap. ${it.numero} (${it.corazones})" }
        )

        Surface(
            color = colors.cardBg,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Likes vs Dislikes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("• Likes: ${novela.meGusta} (Línea Azul)", fontSize = 12.sp, color = Color(0xFF1E88E5), fontWeight = FontWeight.SemiBold)
                    Text("• Dislikes: ${novela.noMeGusta} (Línea Roja)", fontSize = 12.sp, color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                }
                LienzoGraficaLineas(
                    series = listOf(
                        datosLikes to Color(0xFF1E88E5),
                        datosDislikes to Color(0xFFE53935)
                    ),
                    etiquetas = etiquetasCaps
                )
                Text(
                    text = capitulos.joinToString(" • ") { "Cap. ${it.numero} (👍${it.meGusta} / 👎${it.noMeGusta})" },
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }

        TarjetaGraficaLineaSimple(
            titulo = "Comentarios (💬 ${novela.totalComentarios} en total)",
            valores = datosComentarios,
            colorLinea = Color(0xFF8E44AD),
            pieDetalle = capitulos.joinToString(" • ") { "Cap. ${it.numero} (${it.comentariosCount})" }
        )

        TarjetaGraficaLineaSimple(
            titulo = "Vistas Completas (👁 ${novela.vistas} en total)",
            valores = datosVistas,
            colorLinea = Color(0xFF2E7D32),
            pieDetalle = capitulos.joinToString(" • ") { "Cap. ${it.numero} (${it.vistas})" }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TarjetaGraficaLineaSimple(
    titulo: String,
    valores: List<Float>,
    colorLinea: Color,
    pieDetalle: String
) {
    val colors = LocalReadwoodColors.current
    Surface(
        color = colors.cardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textDark)
            LienzoGraficaLineas(
                series = listOf(valores to colorLinea),
                etiquetas = valores.indices.map { "Cap. ${it + 1}" }
            )
            Text(pieDetalle, fontSize = 11.sp, color = colors.textMuted)
        }
    }
}

@Composable
private fun LienzoGraficaLineas(
    series: List<Pair<List<Float>, Color>>,
    etiquetas: List<String>
) {
    val colors = LocalReadwoodColors.current
    val maxValor = series.flatMap { it.first }.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val w = size.width
        val h = size.height

        for (i in 0..2) {
            val yLine = h * (i / 2f)
            drawLine(
                color = colors.border,
                start = Offset(0f, yLine),
                end = Offset(w, yLine),
                strokeWidth = 1.5f
            )
        }

        series.forEach { (puntos, color) ->
            val coords = if (puntos.size <= 1) {
                val v = puntos.firstOrNull() ?: 0f
                val y = h - ((v / maxValor) * (h * 0.80f)) - (h * 0.08f)
                listOf(Offset(w * 0.15f, y), Offset(w * 0.85f, y))
            } else {
                val stepX = (w * 0.70f) / (puntos.size - 1).coerceAtLeast(1)
                puntos.mapIndexed { idx, v ->
                    val x = w * 0.15f + idx * stepX
                    val y = h - ((v / maxValor) * (h * 0.80f)) - (h * 0.08f)
                    Offset(x, y)
                }
            }

            if (coords.size >= 2) {
                val path = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        lineTo(coords[i].x, coords[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            coords.forEach { pt ->
                drawCircle(color = color, radius = 4.dp.toPx(), center = pt)
            }
        }
    }
}