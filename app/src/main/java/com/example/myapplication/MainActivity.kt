package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.myapplication.ui.theme.ReadwoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadwoodTheme {
                var pantallaActual by remember { mutableStateOf("login") }

                when (pantallaActual) {
                    "login" -> {
                        LoginScreen(
                            onLoginExitoso = {
                                pantallaActual = "home"
                            }
                        )
                    }
                    "home" -> {
                        HomeScreen(
                            onAbrirNotificaciones = {
                            },
                            onSeleccionarNovela = { novela ->
                            }
                        )
                    }
                }
            }
        }
    }
}