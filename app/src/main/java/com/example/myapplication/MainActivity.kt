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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var seccionActual by remember { mutableStateOf("HOME") }

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        // 1. Casa -> Home
                        NavigationBarItem(
                            selected = seccionActual == "HOME",
                            onClick = { seccionActual = "HOME" },
                            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                            label = { Text("Descubrir") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ReadwoodPurple,
                                selectedTextColor = ReadwoodPurple,
                                indicatorColor = ReadwoodCardBg,
                                unselectedIconColor = ReadwoodTextMuted,
                                unselectedTextColor = ReadwoodTextMuted
                            )
                        )

                        // 2. Biblioteca (Usamos DateRange que está en el paquete base de Jetpack Compose)
                        NavigationBarItem(
                            selected = seccionActual == "BIBLIOTECA",
                            onClick = { seccionActual = "BIBLIOTECA" },
                            icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Biblioteca") },
                            label = { Text("Biblioteca") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ReadwoodPurple,
                                selectedTextColor = ReadwoodPurple,
                                indicatorColor = ReadwoodCardBg,
                                unselectedIconColor = ReadwoodTextMuted,
                                unselectedTextColor = ReadwoodTextMuted
                            )
                        )

                        // 3. Lápiz -> Escritura
                        NavigationBarItem(
                            selected = seccionActual == "ESCRITURA",
                            onClick = { seccionActual = "ESCRITURA" },
                            icon = { Icon(Icons.Outlined.Create, contentDescription = "Escribir") },
                            label = { Text("Escribir") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ReadwoodPurple,
                                selectedTextColor = ReadwoodPurple,
                                indicatorColor = ReadwoodCardBg,
                                unselectedIconColor = ReadwoodTextMuted,
                                unselectedTextColor = ReadwoodTextMuted
                            )
                        )

                        // 4. Círculo -> Perfil
                        NavigationBarItem(
                            selected = seccionActual == "PERFIL",
                            onClick = { seccionActual = "PERFIL" },
                            icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Perfil") },
                            label = { Text("Perfil") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ReadwoodPurple,
                                selectedTextColor = ReadwoodPurple,
                                indicatorColor = ReadwoodCardBg,
                                unselectedIconColor = ReadwoodTextMuted,
                                unselectedTextColor = ReadwoodTextMuted
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (seccionActual) {
                        "HOME" -> PapasScreen()
                        "BIBLIOTECA" -> Text("Pantalla Biblioteca (Pendiente)", modifier = Modifier.padding(16.dp))
                        "ESCRITURA" -> Text("Pantalla Escritura (Pendiente)", modifier = Modifier.padding(16.dp))
                        "PERFIL" -> Text("Pantalla Perfil (Pendiente)", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}