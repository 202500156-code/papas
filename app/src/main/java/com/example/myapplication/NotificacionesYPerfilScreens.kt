package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

@Composable
fun NotificacionesScreen(
    notificaciones: List<NotificacionItem>,
    onVolver: () -> Unit
) {
    val colors = LocalReadwoodColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onVolver) { Text("← Volver", color = colors.purple, fontWeight = FontWeight.Bold) }
            Text("Notificaciones", fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textDark)
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notificaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones activas.", color = colors.textMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notificaciones, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.mensaje, color = colors.textDark, fontSize = 14.sp, fontWeight = if (!item.leida) FontWeight.Bold else FontWeight.Normal)
                                Text(item.fecha, color = colors.textMuted, fontSize = 11.sp)
                            }
                            if (!item.leida) {
                                TextButton(onClick = { FirestoreService.marcarNotificacionLeida(item.id) }) {
                                    Text("Leída", color = colors.purple, fontSize = 12.sp)
                                }
                            }
                            // Borrado lógico: marca eliminada = true en Firebase sin destruir el registro
                            IconButton(onClick = { FirestoreService.borradoLogicoNotificacion(item.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Ocultar notificación", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilScreen(
    usuario: UsuarioPerfil,
    misNovelas: List<Novela>,
    seguimientos: List<SeguimientoRelacion>,
    modoOscuro: Boolean,
    onToggleModoOscuro: (Boolean) -> Unit,
    onAbrirApiYGifs: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    var ocultarSeguidos by remember { mutableStateOf(usuario.ocultarSeguidos) }
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }

    if (mostrarConfirmacionSalir) {
        ConfirmDialog(
            title = "¿Deseas cerrar tu sesión en Readwood?",
            onConfirm = {
                mostrarConfirmacionSalir = false
                onCerrarSesion()
            },
            onDismiss = { mostrarConfirmacionSalir = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Mi Perfil", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.textDark)

        // Tarjeta de identidad (ID + Nombre)
        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(usuario.usuario, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.purple)
                Text("ID de Usuario: ${usuario.id}", fontSize = 11.sp, color = colors.textMuted)
                Text("Correo: ${usuario.correo}", fontSize = 12.sp, color = colors.textMuted)
            }
        }

        // Progreso de tus novelas en el perfil
        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Progreso de Mis Novelas", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (misNovelas.isEmpty()) {
                    Text("Aún no tienes novelas creadas.", fontSize = 13.sp, color = colors.textMuted)
                } else {
                    misNovelas.forEach { nov ->
                        val capsPublicados = nov.capitulos.count { !it.esBorrador }
                        val totalCaps = nov.capitulos.size.coerceAtLeast(1)
                        val ratio = capsPublicados.toFloat() / totalCaps.toFloat()
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(nov.titulo, fontWeight = FontWeight.SemiBold, color = colors.textDark, fontSize = 14.sp)
                                Text("$capsPublicados/$totalCaps cap. pub. • 👁 ${nov.vistas}", fontSize = 12.sp, color = colors.textMuted)
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                color = colors.purple
                            )
                        }
                    }
                }
            }
        }

        // Tabla relacional: A quién sigues y qué novela sigues (Usuario ID -> Seguido + Novela)
        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("A quién sigo y Novelas (${seguimientos.size})", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 15.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Ocultar mi lista de seguidos a otros", fontSize = 13.sp, color = colors.textDark)
                    Switch(checked = ocultarSeguidos, onCheckedChange = {
                        ocultarSeguidos = it
                        FirestoreService.actualizarPrivacidadSeguidos(usuario.id, it)
                    })
                }
                Spacer(modifier = Modifier.height(8.dp))
                seguimientos.forEach { seg ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Autor: ${seg.seguidoAutorNombre}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textDark)
                            Text("Novela: ${seg.novelaTitulo} (${seg.progresoPorcentaje}%)", fontSize = 12.sp, color = colors.textMuted)
                        }
                        TextButton(onClick = { FirestoreService.alternarOcultarSeguimiento(seg.id, !seg.oculta) }) {
                            Text(if (seg.oculta) "🔒 Oculta" else "Visible", fontSize = 11.sp, color = colors.purple)
                        }
                    }
                }
            }
        }

        // Apariencia (Modo Claro / Oscuro) y acceso al módulo final de GIFs y APIs
        Card(colors = CardDefaults.cardColors(containerColor = colors.cardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ajustes de Aplicación", fontWeight = FontWeight.Bold, color = colors.purple, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Modo Oscuro Editorial", color = colors.textDark, fontSize = 14.sp)
                    Switch(checked = modoOscuro, onCheckedChange = onToggleModoOscuro)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onAbrirApiYGifs, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir Galería de GIFs y Llamadas a APIs", color = colors.purple)
                }
            }
        }

        Button(
            onClick = { mostrarConfirmacionSalir = true },
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}