package com.example.myapplication

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import com.example.myapplication.ui.theme.ReadwoodTheme
import org.json.JSONArray
import org.json.JSONObject

fun SessionManager.guardarUsuarioActual(context: Context, usuario: UsuarioPerfil) {
    val prefs = context.getSharedPreferences("readwood_session_ext", Context.MODE_PRIVATE)
    val obj = JSONObject().apply {
        put("id", usuario.id)
        put("usuario", usuario.usuario)
        put("correo", usuario.correo)
        put("mostrarCorreoPublico", usuario.mostrarCorreoPublico)
        put("fotoBase64", usuario.fotoBase64)
        put("consintioFoto", usuario.consintioFoto)
        put("bannerBase64", usuario.bannerBase64)
        put("bannerColorHex", usuario.bannerColorHex)
        put("enlacesRedes", JSONArray(usuario.enlacesRedes))
    }
    prefs.edit().putString("usuario_actual_json", obj.toString()).apply()
}

fun SessionManager.obtenerUsuarioActual(context: Context): UsuarioPerfil? {
    val prefs = context.getSharedPreferences("readwood_session_ext", Context.MODE_PRIVATE)
    val raw = prefs.getString("usuario_actual_json", null) ?: return null
    return try {
        val obj = JSONObject(raw)
        val arrRedes = obj.optJSONArray("enlacesRedes") ?: JSONArray()
        val listaRedes = mutableListOf<String>()
        for (i in 0 until arrRedes.length()) {
            listaRedes.add(arrRedes.optString(i))
        }
        UsuarioPerfil(
            id = obj.optString("id"),
            usuario = obj.optString("usuario"),
            correo = obj.optString("correo"),
            mostrarCorreoPublico = obj.optBoolean("mostrarCorreoPublico", false),
            fotoBase64 = obj.optString("fotoBase64"),
            consintioFoto = obj.optBoolean("consintioFoto", false),
            bannerBase64 = obj.optString("bannerBase64"),
            bannerColorHex = obj.optString("bannerColorHex", "#5E2D40"),
            enlacesRedes = listaRedes
        )
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (UsuarioPerfil) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    var esRegistro by remember { mutableStateOf(false) }
    var usuarioInput by remember { mutableStateOf("") }
    var correoInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var fotoBase64 by remember { mutableStateOf("") }
    var consintioFoto by remember { mutableStateOf(false) }
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }

    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var mensajeInfo by remember { mutableStateOf("") }

    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var emailRecuperacion by remember { mutableStateOf("") }
    var correoEnviadoExito by remember { mutableStateOf(false) }
    var usuarioRecuperado by remember { mutableStateOf("") }
    var errorRecuperacion by remember { mutableStateOf("") }
    var cargandoRecuperacion by remember { mutableStateOf(false) }

    val selectorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uriPendiente = uri
    }

    if (uriPendiente != null) {
        AlertDialog(
            onDismissRequest = { uriPendiente = null },
            title = { Text("Consentimiento de Foto de Perfil") },
            text = { Text("¿Autorizas guardar esta foto como tu imagen de perfil en Readwood?") },
            confirmButton = {
                Button(onClick = {
                    val b64 = ImageUtils.uriToCompressedBase64(context, uriPendiente!!)
                    if (b64.isNotEmpty()) {
                        fotoBase64 = b64
                        consintioFoto = true
                    }
                    uriPendiente = null
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { uriPendiente = null }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRecuperar = false
                errorRecuperacion = ""
            },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = if (correoEnviadoExito) "Correo Enviado" else "Recuperar Contraseña",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.purple
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!correoEnviadoExito) {
                        Text(
                            text = "Ingresa el correo electrónico vinculado a tu cuenta. Google Firebase te enviará un enlace oficial para crear tu nueva contraseña.",
                            fontSize = 13.sp,
                            color = colors.textDark
                        )
                        OutlinedTextField(
                            value = emailRecuperacion,
                            onValueChange = { emailRecuperacion = it; errorRecuperacion = "" },
                            label = { Text("Correo electrónico (Gmail)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Surface(
                            color = colors.purple.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, colors.purple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✉ Se envió un enlace oficial de Google Firebase a $emailRecuperacion.\n\nAbre tu correo (revisa también Spam), cambia tu contraseña en el enlace y luego inicia sesión aquí con tu usuario ($usuarioRecuperado) y tu nueva contraseña.",
                                fontSize = 13.sp,
                                color = colors.textDark,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (errorRecuperacion.isNotEmpty()) {
                        Text(errorRecuperacion, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !cargandoRecuperacion,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                    onClick = {
                        if (!correoEnviadoExito) {
                            if (emailRecuperacion.isBlank() || !emailRecuperacion.contains("@")) {
                                errorRecuperacion = "Ingresa un correo electrónico válido."
                                return@Button
                            }
                            cargandoRecuperacion = true
                            FirestoreService.enviarCorreoRecuperacionFirebase(
                                correo = emailRecuperacion,
                                onSuccess = { userEncontrado ->
                                    cargandoRecuperacion = false
                                    usuarioRecuperado = userEncontrado
                                    correoEnviadoExito = true
                                },
                                onError = { err ->
                                    cargandoRecuperacion = false
                                    errorRecuperacion = err
                                }
                            )
                        } else {
                            usuarioInput = usuarioRecuperado
                            passwordInput = ""
                            mensajeError = ""
                            mensajeInfo = "Cambia tu contraseña desde el enlace enviado a tu correo e inicia sesión."
                            mostrarDialogoRecuperar = false
                        }
                    }
                ) {
                    Text(
                        text = when {
                            cargandoRecuperacion -> "Enviando..."
                            correoEnviadoExito -> "Entendido, volver al Login"
                            else -> "Enviar correo"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (!correoEnviadoExito) {
                    TextButton(
                        onClick = {
                            mostrarDialogoRecuperar = false
                            errorRecuperacion = ""
                        }
                    ) {
                        Text("Cancelar", color = colors.textMuted)
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoapp),
            contentDescription = "Logo Readwood",
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Readwood",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            color = colors.purple
        )
        Text(
            text = "Tu refugio literario",
            fontSize = 14.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = colors.cardBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.8.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (esRegistro) "Crear Cuenta" else "Iniciar Sesión",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.textDark
                )

                if (esRegistro) {
                    val fotoBmp = remember(fotoBase64) { ImageUtils.base64ToImageBitmap(fotoBase64) }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.bg)
                            .border(1.5.dp, colors.purple, CircleShape)
                            .clickable { selectorGaleria.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoBmp != null) {
                            Image(bitmap = fotoBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = colors.purple, modifier = Modifier.size(44.dp))
                        }
                    }
                    Text("Toca para elegir foto de perfil (opcional)", fontSize = 11.sp, color = colors.textMuted)
                }

                OutlinedTextField(
                    value = usuarioInput,
                    onValueChange = { usuarioInput = it; mensajeError = ""; mensajeInfo = "" },
                    label = { Text("Nombre de usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (esRegistro) {
                    OutlinedTextField(
                        value = correoInput,
                        onValueChange = { correoInput = it; mensajeError = "" },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; mensajeError = ""; mensajeInfo = "" },
                    label = { Text(if (esRegistro) "Contraseña (mín. 6 caracteres)" else "Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (mensajeInfo.isNotEmpty()) {
                    Text(
                        text = mensajeInfo,
                        color = colors.purple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                if (mensajeError.isNotEmpty()) {
                    Text(mensajeError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                if (!esRegistro) {
                    TextButton(
                        onClick = {
                            emailRecuperacion = ""
                            correoEnviadoExito = false
                            errorRecuperacion = ""
                            mostrarDialogoRecuperar = true
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña? Cámbiala con tu correo",
                            color = colors.purple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Button(
                    onClick = {
                        mensajeError = ""
                        mensajeInfo = ""
                        if (usuarioInput.isBlank() || passwordInput.isBlank() || (esRegistro && correoInput.isBlank())) {
                            mensajeError = "Completa todos los campos obligatorios."
                            return@Button
                        }
                        cargando = true
                        if (esRegistro) {
                            FirestoreService.registrarUsuario(
                                usuario = usuarioInput,
                                correo = correoInput,
                                passwordPlano = passwordInput,
                                fotoBase64 = fotoBase64,
                                consintioFoto = consintioFoto,
                                onSuccess = {
                                    cargando = false
                                    onAuthSuccess(it)
                                },
                                onError = {
                                    cargando = false
                                    mensajeError = it
                                }
                            )
                        } else {
                            FirestoreService.iniciarSesion(
                                usuario = usuarioInput,
                                passwordPlano = passwordInput,
                                onSuccess = {
                                    cargando = false
                                    onAuthSuccess(it)
                                },
                                onError = {
                                    cargando = false
                                    mensajeError = it
                                }
                            )
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (cargando) "Cargando..." else if (esRegistro) "Registrarme" else "Entrar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = {
                    esRegistro = !esRegistro
                    mensajeError = ""
                    mensajeInfo = ""
                }) {
                    Text(
                        text = if (esRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿Nuevo en Readwood? Crea una cuenta",
                        color = colors.purple,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var usuarioActual by remember { mutableStateOf(SessionManager.obtenerUsuarioActual(context)) }
            var modoOscuro by remember { mutableStateOf(SessionManager.obtenerModoOscuro(context)) }

            ReadwoodTheme(darkTheme = modoOscuro) {
                if (usuarioActual == null) {
                    AuthScreen(
                        onAuthSuccess = { perfil ->
                            SessionManager.guardarUsuarioActual(context, perfil)
                            usuarioActual = perfil
                        }
                    )
                } else {
                    MainAppNavigation(
                        usuarioInicial = usuarioActual!!,
                        modoOscuro = modoOscuro,
                        onCambiarModoOscuro = { nuevoModo ->
                            modoOscuro = nuevoModo
                            SessionManager.guardarModoOscuro(context, nuevoModo)
                        },
                        onCerrarSesion = {
                            context.getSharedPreferences("readwood_session_ext", Context.MODE_PRIVATE).edit().clear().apply()
                            SessionManager.cerrarSesion(context)
                            usuarioActual = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation(
    usuarioInicial: UsuarioPerfil,
    modoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalReadwoodColors.current

    var usuario by remember { mutableStateOf(usuarioInicial) }
    var tabActual by remember { mutableIntStateOf(0) }
    var novelas by remember { mutableStateOf(emptyList<Novela>()) }
    var seguimientos by remember { mutableStateOf(emptyList<SeguimientoRelacion>()) }
    var usuariosQueSigo by remember { mutableStateOf(emptyList<UsuarioSeguimiento>()) }
    var notificaciones by remember { mutableStateOf(emptyList<NotificacionItem>()) }
    val idsNotificados = remember { mutableSetOf<String>() }

    var novelaDetalleId by remember { mutableStateOf<String?>(null) }
    var novelaLeyendoId by remember { mutableStateOf<String?>(null) }
    var indiceCapituloLeyendo by remember { mutableIntStateOf(0) }
    var mostrandoNotificaciones by remember { mutableStateOf(false) }
    var novelaParaEditar by remember { mutableStateOf<Novela?>(null) }
    var perfilAutorVisitadoId by remember { mutableStateOf<String?>(null) }

    val launcherPermisoNotif = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        NotificationHelper.crearCanal(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcherPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    BackHandler(
        enabled = tabActual != 0 &&
                novelaDetalleId == null &&
                novelaLeyendoId == null &&
                !mostrandoNotificaciones &&
                perfilAutorVisitadoId == null
    ) {
        tabActual = 0
    }

    DisposableEffect(usuario.id) {
        val regPerfil = FirestoreService.escucharPerfilUsuario(usuario.id) { perfilActualizado ->
            usuario = perfilActualizado
            SessionManager.guardarUsuarioActual(context, perfilActualizado)
        }
        val regNovelas = FirestoreService.escucharNovelas(usuario.id) { novelas = it }
        val regSeg = FirestoreService.escucharSeguimientosUsuario(usuario.id) { seguimientos = it }
        val regUsersSeg = FirestoreService.escucharUsuariosQueSigo(usuario.id) { usuariosQueSigo = it }
        val regNotif = FirestoreService.escucharNotificacionesActivas(usuario.id) { listaNotifs ->
            notificaciones = listaNotifs
            listaNotifs.filter { !it.leida && it.id !in idsNotificados }.forEach { nueva ->
                idsNotificados.add(nueva.id)
                NotificationHelper.mostrarNotificacionSistema(
                    context = context,
                    id = nueva.id.hashCode(),
                    titulo = "Readwood",
                    mensaje = nueva.mensaje
                )
            }
        }
        onDispose {
            regPerfil.remove()
            regNovelas.remove()
            regSeg.remove()
            regUsersSeg.remove()
            regNotif.remove()
        }
    }

    val novelaDetalle = novelas.find { it.id == novelaDetalleId }
        ?: OfflineStorage.obtenerNovelasOffline(context).find { it.id == novelaDetalleId }
    val novelaLeyendo = novelas.find { it.id == novelaLeyendoId }
        ?: OfflineStorage.obtenerNovelasOffline(context).find { it.id == novelaLeyendoId }

    val sinLeer = notificaciones.count { !it.leida }

    when {
        novelaLeyendo != null -> {
            LecturaScreen(
                usuario = usuario,
                novela = novelaLeyendo,
                indiceCapituloInicial = indiceCapituloLeyendo,
                seguimientos = seguimientos,
                modoOscuroApp = modoOscuro,
                novelas = novelas,
                onSeleccionarNovela = { nuevaNov ->
                    novelaLeyendoId = null
                    novelaDetalleId = nuevaNov.id
                },
                onVolver = { novelaLeyendoId = null },
                onAbrirPerfilAutor = { idAutor ->
                    novelaLeyendoId = null
                    if (idAutor != usuario.id) {
                        perfilAutorVisitadoId = idAutor
                    }
                }
            )
        }
        novelaDetalle != null -> {
            DetalleNovelaScreen(
                usuario = usuario,
                novela = novelaDetalle,
                seguimientos = seguimientos,
                novelas = novelas,
                onSeleccionarNovela = { nuevaNov ->
                    novelaDetalleId = nuevaNov.id
                },
                onVolver = { novelaDetalleId = null },
                onLeerCapitulo = { idx ->
                    indiceCapituloLeyendo = idx
                    novelaLeyendoId = novelaDetalle.id
                },
                onAbrirPerfilAutor = { idAutor ->
                    novelaDetalleId = null
                    if (idAutor != usuario.id) {
                        perfilAutorVisitadoId = idAutor
                    }
                },
                onAgregarCapituloANovela = { nov ->
                    novelaDetalleId = null
                    novelaParaEditar = nov
                    tabActual = 3
                }
            )
        }
        perfilAutorVisitadoId != null && perfilAutorVisitadoId != usuario.id -> {
            PerfilPublicoAutorScreen(
                usuarioActual = usuario,
                autorId = perfilAutorVisitadoId!!,
                novelas = novelas,
                usuariosQueSigo = usuariosQueSigo,
                onVolver = { perfilAutorVisitadoId = null },
                onSeleccionarNovela = { nov ->
                    perfilAutorVisitadoId = null
                    novelaDetalleId = nov.id
                },
                onAbrirOtroPerfil = { otroId ->
                    if (otroId != usuario.id) {
                        perfilAutorVisitadoId = otroId
                    }
                }
            )
        }
        mostrandoNotificaciones -> {
            NotificacionesScreen(
                notificaciones = notificaciones,
                onVolver = { mostrandoNotificaciones = false }
            )
        }
        else -> {
            Scaffold(
                containerColor = colors.bg,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar(containerColor = colors.cardBg) {
                        NavigationBarItem(
                            selected = tabActual == 0,
                            onClick = { tabActual = 0 },
                            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                            label = { Text("Inicio", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tabActual == 1,
                            onClick = { tabActual = 1 },
                            icon = { Icon(Icons.Outlined.Face, contentDescription = "Comunidad") },
                            label = { Text("Comunidad", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tabActual == 2,
                            onClick = { tabActual = 2 },
                            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Biblioteca") },
                            label = { Text("Biblioteca", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tabActual == 3,
                            onClick = {
                                novelaParaEditar = null
                                tabActual = 3
                            },
                            icon = { Icon(Icons.Outlined.Create, contentDescription = "Escribir") },
                            label = { Text("Escribir", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tabActual == 4,
                            onClick = { tabActual = 4 },
                            icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Perfil") },
                            label = { Text("Perfil", fontSize = 10.sp) }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (tabActual) {
                        0 -> HomeScreen(
                            usuario = usuario,
                            novelas = novelas,
                            seguimientos = seguimientos,
                            usuariosQueSigo = usuariosQueSigo,
                            notificacionesSinLeer = sinLeer,
                            onAbrirNotificaciones = { mostrandoNotificaciones = true },
                            onSeleccionarNovela = { novelaDetalleId = it.id },
                            onAbrirPerfilAutor = { idAutor ->
                                if (idAutor != usuario.id) perfilAutorVisitadoId = idAutor
                            }
                        )
                        1 -> ComunidadScreen(
                            usuario = usuario,
                            novelas = novelas,
                            onSeleccionarNovela = { novelaDetalleId = it.id },
                            onAbrirPerfilAutor = { idAutor ->
                                if (idAutor != usuario.id) perfilAutorVisitadoId = idAutor
                            }
                        )
                        2 -> BibliotecaScreen(
                            novelas = novelas,
                            seguimientos = seguimientos,
                            usuariosQueSigo = usuariosQueSigo,
                            onSeleccionarNovela = { novelaDetalleId = it.id },
                            onAbrirPerfilAutor = { idAutor ->
                                if (idAutor != usuario.id) perfilAutorVisitadoId = idAutor
                            }
                        )
                        3 -> EscribirScreen(
                            usuario = usuario,
                            misNovelas = novelas.filter { it.autorId == usuario.id },
                            novelaAEditar = novelaParaEditar,
                            onLimpiarEdicion = { novelaParaEditar = null }
                        )
                        4 -> PerfilScreen(
                            usuario = usuario,
                            misNovelas = novelas.filter { it.autorId == usuario.id },
                            usuariosQueSigo = usuariosQueSigo,
                            modoOscuro = modoOscuro,
                            onCambiarModoOscuro = onCambiarModoOscuro,
                            onEditarNovela = {
                                novelaParaEditar = it
                                tabActual = 3
                            },
                            onAbrirPerfilAutor = { idAutor ->
                                if (idAutor != usuario.id) perfilAutorVisitadoId = idAutor
                            },
                            onCerrarSesion = onCerrarSesion
                        )
                    }
                }
            }
        }
    }
}