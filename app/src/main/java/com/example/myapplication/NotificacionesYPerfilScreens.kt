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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
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
    modoOscuro: Boolean,
    onToggleModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    var fotoBase64 by remember(usuario.fotoBase64) { mutableStateOf(usuario.fotoBase64) }
    var mostrarConsentimientoCambioFoto by remember { mutableStateOf(false) }

    var instagram by remember(usuario.redInstagram) { mutableStateOf(usuario.redInstagram) }
    var twitter by remember(usuario.redTwitter) { mutableStateOf(usuario.redTwitter) }
    var consintioRedes by remember(usuario.consintioRedes) { mutableStateOf(usuario.consintioRedes) }
    var mostrarDialogoRedes by remember { mutableStateOf(false) }

    var ocultarSeguidos by remember(usuario.ocultarSeguidos) { mutableStateOf(usuario.ocultarSeguidos) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }

    fun actualizarSesionLocal(
        nuevaFoto: String = fotoBase64,
        nuevoIg: String = instagram,
        nuevoTw: String = twitter,
        nuevoConsentRedes: Boolean = consintioRedes,
        nuevoOcultarSeg: Boolean = ocultarSeguidos
    ) {
        val actualizado = usuario.copy(
            fotoBase64 = nuevaFoto,
            consintioFoto = nuevaFoto.isNotEmpty(),
            redInstagram = nuevoIg.trim(),
            redTwitter = nuevoTw.trim(),
            consintioRedes = nuevoConsentRedes,
            ocultarSeguidos = nuevoOcultarSeg
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
                    "Readwood es una plataforma editorial independiente diseñada para lectores y escritores.\n\n• Sincronización multi-dispositivo encriptada en Firebase.\n• Control total de privacidad sobre borradores, seguidos y redes.\n• Lectura sin conexión almacenada exclusivamente dentro de la app.",
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
                        .border(2.dp, colors.purple, CircleShape)
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
                    Text("ID: ${usuario.id}", fontSize = 11.sp, color = colors.textMuted)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    if (misNovelas.isEmpty()) {
                        Text("Aún no tienes novelas creadas.", fontSize = 13.sp, color = colors.textMuted)
                    } else {
                        misNovelas.forEach { nov ->
                            val capsPublicados = nov.capitulos.count { !it.esBorrador }
                            val totalCaps = nov.capitulos.size.coerceAtLeast(1)
                            val ratio = capsPublicados.toFloat() / totalCaps.toFloat()
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(nov.titulo, fontWeight = FontWeight.SemiBold, color = colors.textDark, fontSize = 14.sp)
                                    Text(
                                        "$capsPublicados/$totalCaps cap. • ♥ ${nov.meGusta} • 👁 ${nov.vistas}",
                                        fontSize = 12.sp,
                                        color = colors.textMuted
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    color = colors.purple
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ajustes de Cuenta", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 16.sp)

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
                        Text("Ocultar mi lista de seguidos (${seguimientos.size})", color = colors.textDark, fontSize = 14.sp)
                        Switch(checked = ocultarSeguidos, onCheckedChange = {
                            ocultarSeguidos = it
                            FirestoreService.actualizarPrivacidadSeguidos(usuario.id, it)
                            actualizarSesionLocal(nuevoOcultarSeg = it)
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