package com.example.myapplication

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.ReadwoodDarkButton

@Composable
fun WriterScreen() {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Publicar capitulo")
    }

    if (showDialog) {
        ConfirmDialog(
            title = "¿Deseas publicar este capitulo?",
            onConfirm = {
                showDialog = false
                // Lógica de guardado en Firestore
            },
            onDismiss = { showDialog = false }
        )
    }
}