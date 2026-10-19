package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var usuario by remember { mutableStateOf("") }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Configuración de Cuenta", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Cambiar nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passActual,
            onValueChange = { passActual = it },
            label = { Text("Contraseña actual") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passNueva,
            onValueChange = { passNueva = it },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = { /* Lógica para cambiar de cuenta */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar de cuenta")
        }

        Button(
            onClick = { showLogoutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "¿Seguro que deseas cerrar la sesión?",
            onConfirm = {
                showLogoutDialog = false
                // Cerrar sesión
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}