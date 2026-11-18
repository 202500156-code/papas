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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
                    errorMsg = "Ingresa tu usuario y contraseña."
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

    var leyoTerminos by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var mostrarModalTerminos by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // Modal de lectura obligatoria de Términos y Condiciones
    if (mostrarModalTerminos) {
        val scrollTerminos = rememberScrollState()
        val llegoAlFinal by remember {
            derivedStateOf {
                scrollTerminos.maxValue == 0 || scrollTerminos.value >= (scrollTerminos.maxValue - 20)
            }
        }

        AlertDialog(
            onDismissRequest = { mostrarModalTerminos = false },
            containerColor = colors.cardBg,
            title = {
                Text(
                    text = "Términos Editoriales de Readwood",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.purple
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(scrollTerminos)
                ) {
                    Text(
                        text = """
1. Propiedad Intelectual y Obras Originales
Cada autor conserva el 100% de los derechos sobre las novelas, capítulos y borradores publicados o guardados en Readwood. Queda prohibido subir obras de terceros sin autorización.

2. Privacidad de Borradores y Biblioteca
Los borradores marcados como privados son visibles exclusivamente para el autor autenticado. Asimismo, puedes ocultar de tu perfil público cualquier novela o autor que sigas.

3. Descargas Internas de Lectura
Las obras o capítulos guardados para lectura sin conexión permanecen cifrados dentro del almacenamiento privado de la aplicación y no pueden distribuirse fuera de Readwood.

4. Convivencia y Comentarios
Los comentarios en los capítulos se publican en tiempo real. No se permite el acoso, spam ni contenido ilegal.

5. Seguridad de Cuenta
Tu contraseña se almacena mediante hash criptográfico con sal aleatoria en Firebase para proteger tu acceso desde cualquier dispositivo.
                        """.trimIndent(),
                        fontSize = 13.sp,
                        color = colors.textDark,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (!llegoAlFinal) {
                        Text(
                            text = "↓ Desliza hasta el final para habilitar la aceptación",
                            fontSize = 11.sp,
                            color = colors.purple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        leyoTerminos = true
                        aceptaTerminos = true
                        mostrarModalTerminos = false
                        errorMsg = ""
                    },
                    enabled = llegoAlFinal,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) {
                    Text("He leído y acepto", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalTerminos = false }) {
                    Text("Cerrar", color = colors.textMuted)
                }
            }
        )
    }

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
            onValueChange = { correo = it; errorMsg = "" },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; errorMsg = "" },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            label = { Text("Contraseña (encriptada en Firebase)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Fila de Términos: obliga a abrir y leer antes de marcar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = aceptaTerminos,
                onCheckedChange = { marcado ->
                    if (!leyoTerminos) {
                        mostrarModalTerminos = true
                    } else {
                        aceptaTerminos = marcado
                        errorMsg = ""
                    }
                }
            )
            val textoTerminos = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.textMuted, fontSize = 12.sp)) {
                    append("He leído y acepto los ")
                }
                withStyle(
                    SpanStyle(
                        color = colors.purple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("términos editoriales de Readwood")
                }
            }
            Text(
                text = textoTerminos,
                modifier = Modifier.clickable { mostrarModalTerminos = true }
            )
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    correo.isBlank() || !correo.contains("@") ->
                        errorMsg = "Ingresa un correo electrónico válido."
                    usuario.isBlank() ->
                        errorMsg = "Ingresa un nombre de usuario."
                    password.length < 4 ->
                        errorMsg = "La contraseña debe tener al menos 4 caracteres."
                    !leyoTerminos || !aceptaTerminos -> {
                        errorMsg = "Debes abrir y leer los términos editoriales antes de continuar."
                        mostrarModalTerminos = true
                    }
                    else -> {
                        cargando = true
                        FirestoreService.registrarUsuario(
                            usuario = usuario,
                            correo = correo,
                            passwordPlano = password,
                            onSuccess = { cargando = false; onRegistroExitoso(it) },
                            onError = { cargando = false; errorMsg = it }
                        )
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text("Registrarme", color = Color.White, fontWeight = FontWeight.Bold)
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