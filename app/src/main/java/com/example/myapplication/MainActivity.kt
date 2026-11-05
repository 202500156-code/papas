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
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*

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

    // Cambiado de NovelaItem? a Novela? para coincidir con tu modelo real
    var novelaParaLeer by remember { mutableStateOf<Novela?>(null) }
    var viendoNotificaciones by remember { mutableStateOf(false) }

    when (estadoAuth) {
        AuthState.LOGIN -> {
            LoginScreen(
                onLoginExitoso = { estadoAuth = AuthState.AUTENTICADO },
                onIrARegistro = { estadoAuth = AuthState.REGISTRO },
                onIrARecuperar = { estadoAuth = AuthState.RECUPERAR }
            )
        }
        AuthState.REGISTRO -> {
            RegistroScreen(
                onRegistroExitoso = { estadoAuth = AuthState.AUTENTICADO },
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
                    genero = novelaParaLeer!!.categoria, // Se usa categoria en lugar de genero
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
                                onCerrarSesion = {
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