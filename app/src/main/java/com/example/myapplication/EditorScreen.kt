package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditorScreen(
    onPublicarExitoso: () -> Unit = {}
) {
    var titulo by remember { mutableStateOf("") }
    var sinopsis by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Crear Obra", style = MaterialTheme.typography.titleLarge)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
                .clickable { /* Cargar imagen */ },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Subir portada de la obra", color = Color.White)
            }
        }

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título de la obra") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sinopsis,
            onValueChange = { sinopsis = it },
            label = { Text("Sinopsis / Resumen") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar capítulo")
        }
    }

    if (showDialog) {
        ConfirmDialog(
            title = "¿Deseas publicar este capítulo?",
            onConfirm = {
                showDialog = false
                onPublicarExitoso()
            },
            onDismiss = { showDialog = false }
        )
    }
}