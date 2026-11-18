package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.LocalReadwoodColors

@Composable
fun LoginScreen(
    onLoginExitoso: (UsuarioPerfil) -> Unit,
    onIrARegistro: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoapp),
            contentDescription = "Logo Readwood",
            modifier = Modifier.size(90.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Readwood",
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = colors.purple
        )
        Text(
            text = "Tu biblioteca sincronizada en cualquier dispositivo",
            fontSize = 13.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; errorMsg = "" },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (usuario.isBlank() || password.isBlank()) {
                    errorMsg = "Completa ambos campos"
                    return@Button
                }
                cargando = true
                FirestoreService.iniciarSesion(
                    usuario = usuario,
                    passwordPlano = password,
                    onSuccess = { cargando = false; onLoginExitoso(it) },
                    onError = { cargando = false; errorMsg = it }
                )
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Iniciar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿No tienes cuenta? Regístrate aquí",
            color = colors.purple,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onIrARegistro() }
        )
    }
}

@Composable
fun RegistroScreen(
    onRegistroExitoso: (UsuarioPerfil) -> Unit,
    onIrALogin: () -> Unit
) {
    val colors = LocalReadwoodColors.current
    var correo by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var aceptaTerminos by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear cuenta en Readwood",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = colors.textDark
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (encriptada en Firebase)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = aceptaTerminos, onCheckedChange = { aceptaTerminos = it })
            Text("Acepto los términos editoriales de Readwood", fontSize = 12.sp, color = colors.textMuted)
        }

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (correo.isBlank() || usuario.isBlank() || password.length < 4 || !aceptaTerminos) {
                    errorMsg = "Verifica los datos y términos (mín. 4 caracteres)"
                    return@Button
                }
                cargando = true
                FirestoreService.registrarUsuario(
                    usuario = usuario,
                    correo = correo,
                    passwordPlano = password,
                    onSuccess = { cargando = false; onRegistroExitoso(it) },
                    onError = { cargando = false; errorMsg = it }
                )
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrarme", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ya tengo cuenta • Volver a Login",
            color = colors.purple,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onIrALogin() }
        )
    }
}