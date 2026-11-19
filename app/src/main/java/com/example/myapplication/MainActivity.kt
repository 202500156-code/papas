package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.LocalReadwoodColors
import com.example.myapplication.ui.theme.ReadwoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var modoOscuro by remember { mutableStateOf(SessionManager.obtenerModoOscuro(context)) }

            ReadwoodTheme(darkTheme = modoOscuro) {
                MainAppNavigation(
                    modoOscuro = modoOscuro,
                    onToggleModoOscuro = { nuevoValor ->
                        modoOscuro = nuevoValor
                        SessionManager.guardarModoOscuro(context, nuevoValor)
                    }
                )
            }
        }
    }
}

enum class AuthScreenState { LOGIN, REGISTRO, AUTENTICADO }

@Composable
fun MainAppNavigation(
    modoOscuro: Boolean,
    onToggleModoOscuro: (Boolean) -> Unit
) {
    val colors = LocalReadwoodColors.current
    val context = LocalContext.current

    val sesionInicial = remember { SessionManager.obtenerSesionGuardada(context) }
    var usuarioActual by remember { mutableStateOf(sesionInicial) }
    var authState by remember {
        mutableStateOf(
            if (sesionInicial != null) AuthScreenState.AUTENTICADO else AuthScreenState.LOGIN
        )
    }

    var seccionActual by remember { mutableStateOf("HOME") }
    var novelaDetalle by remember { mutableStateOf<Novela?>(null) }
    var capituloLecturaIndex by remember { mutableStateOf<Int?>(null) }
    var viendoNotificaciones by remember { mutableStateOf(false) }

    var novelasFirebase by remember { mutableStateOf<List<Novela>>(emptyList()) }
    var seguimientosUsuario by remember { mutableStateOf<List<SeguimientoRelacion>>(emptyList()) }
    var notificacionesActivas by remember { mutableStateOf<List<NotificacionItem>>(emptyList()) }

    DisposableEffect(usuarioActual?.id) {
        val uid = usuarioActual?.id
        if (uid.isNullOrEmpty()) return@DisposableEffect onDispose {}

        val regPerfil = FirestoreService.escucharPerfilUsuario(uid) { perfilActualizado ->
            usuarioActual = perfilActualizado
            SessionManager.guardarSesion(context, perfilActualizado)
        }
        val regNovelas = FirestoreService.escucharNovelas(uid) { novelasFirebase = it }
        val regSeguidos = FirestoreService.escucharSeguimientosUsuario(uid) { seguimientosUsuario = it }
        val regNotifs = FirestoreService.escucharNotificacionesActivas(uid) { notificacionesActivas = it }

        onDispose {
            regPerfil.remove()
            regNovelas.remove()
            regSeguidos.remove()
            regNotifs.remove()
        }
    }

    when (authState) {
        AuthScreenState.LOGIN -> LoginScreen(
            onLoginExitoso = { perfil ->
                SessionManager.guardarSesion(context, perfil)
                usuarioActual = perfil
                authState = AuthScreenState.AUTENTICADO
            },
            onIrARegistro = { authState = AuthScreenState.REGISTRO }
        )
        AuthScreenState.REGISTRO -> RegistroScreen(
            onRegistroExitoso = { perfil ->
                SessionManager.guardarSesion(context, perfil)
                usuarioActual = perfil
                authState = AuthScreenState.AUTENTICADO
            },
            onIrALogin = { authState = AuthScreenState.LOGIN }
        )
        AuthScreenState.AUTENTICADO -> {
            val user = usuarioActual!!
            val misNovelas = novelasFirebase.filter { it.autorId == user.id }
            val novelaActiva = novelasFirebase.find { it.id == novelaDetalle?.id } ?: novelaDetalle

            when {
                viendoNotificaciones -> NotificacionesScreen(
                    notificaciones = notificacionesActivas,
                    onVolver = { viendoNotificaciones = false }
                )
                novelaActiva != null && capituloLecturaIndex != null -> LecturaScreen(
                    usuario = user,
                    novela = novelaActiva,
                    indiceCapituloInicial = capituloLecturaIndex!!,
                    seguimientos = seguimientosUsuario,
                    onVolver = { capituloLecturaIndex = null }
                )
                novelaActiva != null -> DetalleNovelaScreen(
                    usuario = user,
                    novela = novelaActiva,
                    seguimientos = seguimientosUsuario,
                    onVolver = { novelaDetalle = null },
                    onLeerCapitulo = { idx -> capituloLecturaIndex = idx }
                )
                else -> Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = colors.cardBg, tonalElevation = 8.dp) {
                            NavigationBarItem(
                                selected = seccionActual == "HOME",
                                onClick = { seccionActual = "HOME" },
                                icon = { Icon(Icons.Outlined.Home, contentDescription = "Descubrir") },
                                label = { Text("Descubrir") }
                            )
                            NavigationBarItem(
                                selected = seccionActual == "BIBLIOTECA",
                                onClick = { seccionActual = "BIBLIOTECA" },
                                icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Biblioteca") },
                                label = { Text("Biblioteca") }
                            )
                            NavigationBarItem(
                                selected = seccionActual == "ESCRITURA",
                                onClick = { seccionActual = "ESCRITURA" },
                                icon = { Icon(Icons.Outlined.Create, contentDescription = "Escribir") },
                                label = { Text("Escribir") }
                            )
                            NavigationBarItem(
                                selected = seccionActual == "PERFIL",
                                onClick = { seccionActual = "PERFIL" },
                                icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Perfil") },
                                label = { Text("Perfil") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (seccionActual) {
                            "HOME" -> HomeScreen(
                                usuario = user,
                                novelas = novelasFirebase,
                                seguimientos = seguimientosUsuario,
                                notificacionesSinLeer = notificacionesActivas.count { !it.leida },
                                onAbrirNotificaciones = { viendoNotificaciones = true },
                                onSeleccionarNovela = { novelaDetalle = it }
                            )
                            "BIBLIOTECA" -> BibliotecaScreen(
                                novelas = novelasFirebase,
                                seguimientos = seguimientosUsuario,
                                onSeleccionarNovela = { novelaDetalle = it }
                            )
                            "ESCRITURA" -> EditorScreen(
                                usuario = user,
                                misNovelas = misNovelas,
                                onAbrirNovela = { novelaDetalle = it }
                            )
                            "PERFIL" -> PerfilScreen(
                                usuario = user,
                                misNovelas = misNovelas,
                                seguimientos = seguimientosUsuario,
                                modoOscuro = modoOscuro,
                                onToggleModoOscuro = onToggleModoOscuro,
                                onCerrarSesion = {
                                    SessionManager.cerrarSesion(context)
                                    usuarioActual = null
                                    novelaDetalle = null
                                    capituloLecturaIndex = null
                                    seccionActual = "HOME"
                                    authState = AuthScreenState.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}