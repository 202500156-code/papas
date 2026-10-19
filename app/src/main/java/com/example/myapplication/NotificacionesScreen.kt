package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

data class NotificacionItem(val id: Int, val mensaje: String, val fecha: String)

@Composable
fun NotificacionesScreen() {
    var listaNotificaciones by remember {
        mutableStateOf(
            listOf(
                NotificacionItem(1, "Alguien comentó en tu novela.", "Hace 5m"),
                NotificacionItem(2, "Nuevo capítulo disponible en tu biblioteca.", "Hace 2h")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Notificaciones",
            fontSize = 24.sp,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (listaNotificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones pendientes.", color = ReadwoodDarkTextMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listaNotificaciones, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.mensaje, color = ReadwoodDarkTextPrimary, fontSize = 14.sp)
                                Text(item.fecha, color = ReadwoodDarkTextMuted, fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = {
                                    listaNotificaciones = listaNotificaciones.filter { it.id != item.id }
                                }
                            ) {
                                Text("Borrar", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}