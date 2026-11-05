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
import com.example.myapplication.ui.theme.*

// Colores privados de respaldo para garantizar que compila inmediatamente en este archivo
private val ReadwoodBg = Color(0xFFF8F3ED)
private val ReadwoodPurple = Color(0xFF6B3A50)
private val ReadwoodTextDark = Color(0xFF2D2329)
private val ReadwoodTextMuted = Color(0xFF8A7E85)
private val ReadwoodBorder = Color(0xFFE0D7CD)

// --- REGISTRO (Pantalla Inicial según Balsamiq) ---
@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodBg)
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
            text = "Hola!!!! Bienvenido a Read Wood",
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Correo electrónico
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            placeholder = { Text("Correo electrónico", color = ReadwoodTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre de usuario
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            placeholder = { Text("Nombre de usuario", color = ReadwoodTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Ingresa una contraseña", color = ReadwoodTextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRegistroExitoso() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ingresar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ya estuve aquí",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodPurple,
            modifier = Modifier.clickable { onIrALogin() }
        )
    }
}

// --- LOGIN ---
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodBg)
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
            text = "Hola de nuevo!!!!!",
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark
        )

        if (errorMensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMensaje, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            placeholder = { Text("Nombre de usuario", color = ReadwoodTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Ingresa una contraseña", color = ReadwoodTextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Olvidé mi contraseña",
            fontSize = 12.sp,
            color = ReadwoodPurple,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onIrARecuperar() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (usuario.isBlank() || password.isBlank()) {
                    errorMensaje = "Tu contraseña o usuario no concuerda"
                } else {
                    onLoginExitoso()
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Ingresar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crear nueva cuenta",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextMuted,
            modifier = Modifier.clickable { onIrARegistro() }
        )
    }
}

// --- RESTABLECER CONTRASEÑA ---
@Composable
fun RecuperarPasswordScreen(onVolverALogin: () -> Unit) {
    var paso by remember { mutableStateOf(1) } // 1: Pedir Correo, 2: Código y Nueva Contraseña
    var correo by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var reafirmaPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logoapp),
            contentDescription = "Logo Readwood",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Restablecer la contraseña",
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodTextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (paso == 1) {
            Text(
                text = "Ingresa tu correo electrónico, se te enviará un código a tu cuenta.",
                fontSize = 13.sp,
                color = ReadwoodTextMuted
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = { Text("Correo electrónico", color = ReadwoodTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                    focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { paso = 2 },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ingresar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "Pon el código y dale a la opción restablecer la contraseña.",
                fontSize = 13.sp,
                color = ReadwoodTextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                placeholder = { Text("Código de verificación", color = ReadwoodTextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                    focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nuevaPassword,
                onValueChange = { nuevaPassword = it },
                placeholder = { Text("Coloca tu nueva contraseña", color = ReadwoodTextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                    focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reafirmaPassword,
                onValueChange = { reafirmaPassword = it },
                placeholder = { Text("Reafirma la contraseña", color = ReadwoodTextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = ReadwoodPurple, unfocusedBorderColor = ReadwoodBorder,
                    focusedTextColor = ReadwoodTextDark, unfocusedTextColor = ReadwoodTextDark
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onVolverALogin() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReadwoodPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cambiar contraseña", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Volver",
            fontSize = 13.sp,
            color = ReadwoodPurple,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onVolverALogin() }
        )
    }
}