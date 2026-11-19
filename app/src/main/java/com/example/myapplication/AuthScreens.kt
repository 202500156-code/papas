package com.example.myapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    var correo by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var fotoBase64 by remember { mutableStateOf("") }
    var consintioFoto by remember { mutableStateOf(false) }
    var mostrarConsentimientoFoto by remember { mutableStateOf(false) }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoBase64 = ImageUtils.uriToCompressedBase64(context, uri)
            consintioFoto = fotoBase64.isNotEmpty()
        }
    }

    var leyoTerminos by remember { mutableStateOf(false) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var mostrarModalTerminos by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    if (mostrarConsentimientoFoto) {
        AlertDialog(
            onDismissRequest = { mostrarConsentimientoFoto = false },
            containerColor = colors.cardBg,
            title = { Text("Permiso de Galería y Perfil", fontWeight = FontWeight.Bold, color = colors.purple) },
            text = {
                Text(
                    "¿Autorizas a Readwood a acceder a tu galería para seleccionar una foto de perfil y mostrarla junto a tus obras y comentarios?",
                    color = colors.textDark,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConsentimientoFoto = false
                        galeriaLauncher.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
                ) { Text("Autorizar y elegir foto", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConsentimientoFoto = false }) {
                    Text("Cancelar", color = colors.textMuted)
                }
            }
        )
    }

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
                Text("Términos Editoriales de Readwood", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.purple)
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(scrollTerminos)) {
                    Text(
                        text = """
1. Propiedad Intelectual: Conservas el 100% de los derechos de tus novelas y portadas diseñadas o subidas.
2. Privacidad de Borradores y Datos: Tus borradores son privados y tu correo nunca se expone públicamente. Solo se muestran tu foto o redes si otorgas consentimiento expreso.
3. Descargas Internas: Las obras guardadas offline permanecen exclusivamente dentro de la aplicación.
4. Seguridad: Tu contraseña se encripta con hash y sal aleatoria en Firebase.
                        """.trimIndent(),
                        fontSize = 13.sp,
                        color = colors.textDark,
                        lineHeight = 19.sp
                    )
                    if (!llegoAlFinal) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("↓ Desliza hasta el final para aceptar", fontSize = 11.sp, color = colors.purple, fontWeight = FontWeight.Bold)
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
                ) { Text("He leído y acepto", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarModalTerminos = false }) { Text("Cerrar", color = colors.textMuted) }
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
        Text("Crear cuenta en Readwood", fontSize = 24.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.textDark)
        Spacer(modifier = Modifier.height(14.dp))

        val bitmap = remember(fotoBase64) { ImageUtils.base64ToImageBitmap(fotoBase64) }
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(2.dp, colors.purple, CircleShape)
                .clickable { mostrarConsentimientoFoto = true },
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = "Foto perfil", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "Elegir foto", tint = colors.purple, modifier = Modifier.size(44.dp))
            }
        }
        Text(
            text = if (bitmap != null) "Cambiar foto de galería" else "+ Subir foto de perfil (Opcional)",
            fontSize = 12.sp,
            color = colors.purple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp).clickable { mostrarConsentimientoFoto = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; errorMsg = "" },
            label = { Text("Correo electrónico (privado)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; errorMsg = "" },
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

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

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = aceptaTerminos,
                onCheckedChange = { marcado ->
                    if (!leyoTerminos) mostrarModalTerminos = true
                    else { aceptaTerminos = marcado; errorMsg = "" }
                }
            )
            val textoTerminos = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.textMuted, fontSize = 12.sp)) { append("He leído y acepto los ") }
                withStyle(SpanStyle(color = colors.purple, fontSize = 12.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append("términos editoriales de Readwood")
                }
            }
            Text(text = textoTerminos, modifier = Modifier.clickable { mostrarModalTerminos = true })
        }

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                when {
                    correo.isBlank() || !correo.contains("@") -> errorMsg = "Ingresa un correo válido."
                    usuario.isBlank() -> errorMsg = "Ingresa un nombre de usuario."
                    password.length < 4 -> errorMsg = "La contraseña debe tener al menos 4 caracteres."
                    !leyoTerminos || !aceptaTerminos -> {
                        errorMsg = "Lee y acepta los términos editoriales primero."
                        mostrarModalTerminos = true
                    }
                    else -> {
                        cargando = true
                        FirestoreService.registrarUsuario(
                            usuario = usuario,
                            correo = correo,
                            passwordPlano = password,
                            fotoBase64 = fotoBase64,
                            consintioFoto = consintioFoto,
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

        Spacer(modifier = Modifier.height(14.dp))

        Text("Ya tengo cuenta • Volver a Login", color = colors.purple, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onIrALogin() })
    }
}