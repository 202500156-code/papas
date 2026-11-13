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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay

// Colores de respaldo del tema Readwood para prevenir fallos de scope
private val ReadwoodBg = Color(0xFFF8F3ED)
private val ReadwoodCardBg = Color(0xFFF0EAE1)
private val ReadwoodPurple = Color(0xFF6B3A50)
private val ReadwoodTextMuted = Color(0xFF8A7E85)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadwoodTheme {
                MainAppNavigation()
            }
        }
    }
}

enum class AuthState { LOGIN, REGISTRO, RECUPERAR, AUTENTICADO }

@Composable
fun MainAppNavigation() {
    var estadoAuth by remember { mutableStateOf(AuthState.LOGIN) }
    var seccionActual by remember { mutableStateOf("HOME") }

    var novelaParaLeer by remember { mutableStateOf<Novela?>(null) }
    var viendoNotificaciones by remember { mutableStateOf(false) }

    var contadorActividad by remember { mutableIntStateOf(0) }
    var velocidadActividad by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(estadoAuth, velocidadActividad) {
        if (estadoAuth == AuthState.AUTENTICADO) {
            while (true) {
                val tiempoEspera = (1000f / velocidadActividad).toLong().coerceAtLeast(50L)
                delay(tiempoEspera)
                contadorActividad++
            }
        }
    }

    when (estadoAuth) {
        AuthState.LOGIN -> {
            LoginScreen(
                onLoginExitoso = {
                    contadorActividad = 0 // Reinicio al ingresar
                    estadoAuth = AuthState.AUTENTICADO
                },
                onIrARegistro = { estadoAuth = AuthState.REGISTRO },
                onIrARecuperar = { estadoAuth = AuthState.RECUPERAR }
            )
        }
        AuthState.REGISTRO -> {
            RegistroScreen(
                onRegistroExitoso = {
                    contadorActividad = 0
                    estadoAuth = AuthState.AUTENTICADO
                },
                onIrALogin = { estadoAuth = AuthState.LOGIN }
            )
        }
        AuthState.RECUPERAR -> {
            RecuperarPasswordScreen(
                onVolverALogin = { estadoAuth = AuthState.LOGIN }
            )
        }
        AuthState.AUTENTICADO -> {
            if (viendoNotificaciones) {
                NotificacionesScreen()
            } else if (novelaParaLeer != null) {
                LecturaScreen(
                    tituloNovela = novelaParaLeer!!.titulo,
                    genero = novelaParaLeer!!.categoria,
                    tituloCapitulo = novelaParaLeer!!.capitulos.firstOrNull()?.titulo ?: "Capítulo 1",
                    onVolver = { novelaParaLeer = null }
                )
            } else {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = ReadwoodCardBg,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = seccionActual == "HOME",
                                onClick = { seccionActual = "HOME" },
                                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                                label = { Text("Descubrir") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ReadwoodPurple,
                                    selectedTextColor = ReadwoodPurple,
                                    indicatorColor = ReadwoodBg,
                                    unselectedIconColor = ReadwoodTextMuted,
                                    unselectedTextColor = ReadwoodTextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = seccionActual == "BIBLIOTECA",
                                onClick = { seccionActual = "BIBLIOTECA" },
                                icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Biblioteca") },
                                label = { Text("Biblioteca") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ReadwoodPurple,
                                    selectedTextColor = ReadwoodPurple,
                                    indicatorColor = ReadwoodBg,
                                    unselectedIconColor = ReadwoodTextMuted,
                                    unselectedTextColor = ReadwoodTextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = seccionActual == "ESCRITURA",
                                onClick = { seccionActual = "ESCRITURA" },
                                icon = { Icon(Icons.Outlined.Create, contentDescription = "Escribir") },
                                label = { Text("Escribir") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ReadwoodPurple,
                                    selectedTextColor = ReadwoodPurple,
                                    indicatorColor = ReadwoodBg,
                                    unselectedIconColor = ReadwoodTextMuted,
                                    unselectedTextColor = ReadwoodTextMuted
                                )
                            )
                            NavigationBarItem(
                                selected = seccionActual == "PERFIL",
                                onClick = { seccionActual = "PERFIL" },
                                icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Perfil") },
                                label = { Text("Perfil") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ReadwoodPurple,
                                    selectedTextColor = ReadwoodPurple,
                                    indicatorColor = ReadwoodBg,
                                    unselectedIconColor = ReadwoodTextMuted,
                                    unselectedTextColor = ReadwoodTextMuted
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (seccionActual) {
                            "HOME" -> HomeScreen(
                                onAbrirNotificaciones = { viendoNotificaciones = true },
                                onSeleccionarNovela = { novela -> novelaParaLeer = novela }
                            )
                            "BIBLIOTECA" -> BuscadorScreen(
                                onSeleccionarNovela = { novela -> novelaParaLeer = novela }
                            )
                            "ESCRITURA" -> EditorScreen(
                                onPublicarExitoso = { seccionActual = "HOME" }
                            )
                            "PERFIL" -> PerfilScreen(
                                contador = contadorActividad,
                                velocidadMultiplicador = velocidadActividad,
                                onCambiarVelocidad = { nuevaVel -> velocidadActividad = nuevaVel },
                                onCerrarSesion = {
                                    contadorActividad = 0 // Reinicio al cerrar sesión
                                    estadoAuth = AuthState.LOGIN
                                    seccionActual = "HOME"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}