package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AvatarUsuario(fotoUrl: String, tamano: Dp = 48.dp) {
    if (fotoUrl.isNotBlank()) {
        AsyncImage(
            model = fotoUrl,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(tamano)
                .clip(CircleShape)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar por defecto",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(tamano)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }
}