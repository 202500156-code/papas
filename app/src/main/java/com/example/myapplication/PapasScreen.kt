package com.example.myapplication

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PapasScreen(
    modifier: Modifier = Modifier
) {
    var textoEntrada by remember { mutableStateOf("") }
    var textoGuardado by remember { mutableStateOf("Hola") }

    // Estados agregados para el Switch y la Alerta
    var switchActivo by remember { mutableStateOf(true) }
    var mostrarAlerta by remember { mutableStateOf(false) }

    // 1. Estado para guardar el ángulo actual de rotación
    var rotacionActual by remember { mutableFloatStateOf(0f) }

    // 2. Transición suave al cambiar el ángulo de rotación
    val anguloAnimado by animateFloatAsState(
        targetValue = rotacionActual,
        animationSpec = tween(durationMillis = 600), // Duración de la animación del giro
        label = "RotacionImagen"
    )

    // Temporizador que actualiza la rotación cada 2 segundos xd
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            rotacionActual += 90f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ReadwoodBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ReadwoodCardBg,
            border = androidx.compose.foundation.BorderStroke(2.dp, ReadwoodPurple),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = textoGuardado,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ReadwoodPurple,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.wrapContentSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.mi_imagen),
                contentDescription = "Imagen de la pantalla perro",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .rotate(anguloAnimado)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, ReadwoodPurple, RoundedCornerShape(12.dp))
            )

            Surface(
                color = ReadwoodPurple,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Holis!!!",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 25.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Entradas de texto o variables",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Habilitar Imprimir",
                fontSize = 14.sp,
                color = ReadwoodTextDark
            )
            Switch(
                checked = switchActivo,
                onCheckedChange = { switchActivo = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF34C759), // Verde Cupertino iOS
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF39393D),
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent
                ),
                modifier = Modifier.scale(0.85f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoEntrada,
            onValueChange = { textoEntrada = it },
            label = { Text("Escribe algo aquí...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = ReadwoodBorder,
                focusedBorderColor = ReadwoodPurple
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (textoEntrada.isNotBlank() && switchActivo) {
                    mostrarAlerta = true
                }
            },
            enabled = switchActivo,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple)
        ) {
            Text(
                text = "imprimir",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    if (mostrarAlerta) {
        AlertDialog(
            onDismissRequest = { mostrarAlerta = false },
            title = {
                Text(text = "Confirmación")
            },
            text = {
                Text(text = "¿Deseas imprimir el texto que pusiste perro: \"$textoEntrada\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        textoGuardado = textoEntrada
                        textoEntrada = ""
                        mostrarAlerta = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarAlerta = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
