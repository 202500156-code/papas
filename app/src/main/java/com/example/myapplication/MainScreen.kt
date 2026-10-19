package com.example.myapplication

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readwood",
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkPrimary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Vuelve a tu historia.",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Un lugar para leer, imaginar y compartir lo que llevas dentro.",
            fontSize = 13.sp,
            color = ReadwoodDarkTextMuted,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Email", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("tu.email@ejemplo.com", color = ReadwoodDarkTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contraseña", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Olvidaste tu contraseña?",
            fontSize = 12.sp,
            color = ReadwoodDarkPrimary,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onIrARecuperar() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginExitoso() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Iniciar sesión", color = Color.White, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text("¿Es tu primera página? ", fontSize = 13.sp, color = ReadwoodDarkTextMuted)
            Text(
                text = "Crea una cuenta",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ReadwoodDarkPrimary,
                modifier = Modifier.clickable { onIrARegistro() }
            )
        }
    }
}

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readwood",
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Las buenas historias empiezan contigo.",
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Nombre", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Email", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Contraseña", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRegistroExitoso() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Crear mi cuenta", color = Color.White, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("¿Ya tienes cuenta? ", fontSize = 13.sp, color = ReadwoodDarkTextMuted)
            Text(
                text = "Inicia sesión",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ReadwoodDarkPrimary,
                modifier = Modifier.clickable { onIrALogin() }
            )
        }
    }
}

@Composable
fun RecuperarPasswordScreen(onVolverALogin: () -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Retoma el hilo.",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Escribe el email de tu cuenta y te enviaremos un enlace.",
            fontSize = 13.sp,
            color = ReadwoodDarkTextMuted,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email de tu cuenta", color = ReadwoodDarkTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ReadwoodDarkSurface,
                unfocusedContainerColor = ReadwoodDarkSurface,
                focusedBorderColor = ReadwoodDarkPrimary,
                unfocusedBorderColor = ReadwoodDarkBorder,
                focusedTextColor = ReadwoodDarkTextPrimary,
                unfocusedTextColor = ReadwoodDarkTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onVolverALogin() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Enviar enlace de recuperación", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Volver al inicio de sesión",
            fontSize = 13.sp,
            color = ReadwoodDarkPrimary,
            modifier = Modifier.clickable { onVolverALogin() }
        )
    }
}

@Composable
fun MainAppScreen(onCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readwood",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sin historias guardadas.",
            fontSize = 16.sp,
            color = ReadwoodDarkTextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCerrarSesion() },
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}