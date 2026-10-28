package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import java.security.MessageDigest
import java.security.SecureRandom

// ==========================================
// 1. MÓDULO CRYPTO CON HASH ESTÁTICO
// ==========================================
object NativeCryptoHelper {

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashWithSalt(text: String, salt: String): String {
        if (text.isEmpty()) return ""
        val combined = text + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        val hashHex = bytes.joinToString("") { "%02x".format(it) }
        return "$salt:$hashHex"
    }

    fun generateBcryptStyleHash(text: String, salt: String): String {
        if (text.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest((text + salt).toByteArray())
        val base64Like = bytes.joinToString("") { "%02x".format(it) }.take(31)
        val salt22 = salt.take(22)
        return "\$2b\$10\$$salt22$base64Like"
    }

    fun verify(passwordInput: String, storedHashWithSalt: String): Boolean {
        if (passwordInput.isEmpty() || storedHashWithSalt.isEmpty() || !storedHashWithSalt.contains(":")) {
            return false
        }
        val parts = storedHashWithSalt.split(":")
        val salt = parts[0]
        val newHash = hashWithSalt(passwordInput, salt)
        return newHash == storedHashWithSalt
    }

    // IMPRESIÓN LIMPIA Y FIJA PARA COPIAR
    fun printFixedForCopying(originalPassword: String, bcryptHash: String) {
        println("\n==============================================")
        println("   DATOS FIJOS PARA COPIAR A BCRYPT WEB")
        println("==============================================")
        println("CONTRASEÑA EN TEXTO PLANO:")
        println(originalPassword)
        println("----------------------------------------------")
        println("HASH ESTRUCTURADO (COPIAR ESTA LÍNEA COMPLETA):")
        println(bcryptHash)
        println("==============================================\n")
    }
}

// ==========================================
// 2. PANTALLA LOGIN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Mantenemos un Salt estático para evitar que el hash cambie solo
    var saltEstático by remember { mutableStateOf(NativeCryptoHelper.generateSalt()) }
    var hashActual by remember { mutableStateOf("") }
    var bcryptFormattedHash by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    val usuarioCorrecto = "nadir"
    val saltGuardado = remember { NativeCryptoHelper.generateSalt() }
    val hashGuardadoBD = remember { NativeCryptoHelper.hashWithSalt("\$2a\$10\$0DChu2iJoUnwrayv7KJKWueGdYOGzVFwP5Yj5.w6sx0r0UR1Dd2bW", saltGuardado) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.foto_portada),
            contentDescription = "Logo Readwood",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .padding(bottom = 12.dp)
        )

        Text(
            text = "Readwood",
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Campo Usuario
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "user:",
                fontSize = 14.sp,
                color = ReadwoodDarkTextMuted,
                modifier = Modifier.width(80.dp)
            )
            OutlinedTextField(
                value = user,
                onValueChange = {
                    user = it
                    mensajeError = ""
                },
                placeholder = { Text("aqui el usuario", color = ReadwoodDarkTextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "password:",
                fontSize = 14.sp,
                color = ReadwoodDarkTextMuted,
                modifier = Modifier.width(80.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    mensajeError = ""

                    // Calculamos con el salt fijo actual
                    hashActual = NativeCryptoHelper.hashWithSalt(it, saltEstático)
                    bcryptFormattedHash = NativeCryptoHelper.generateBcryptStyleHash(it, saltEstático)
                },
                placeholder = { Text("aqui mete la contrasena", color = ReadwoodDarkTextMuted) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.weight(1f),
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
        }

        // Muestra en la app el Hash encriptado y permite fijarlo/imprimirlo
        if (bcryptFormattedHash.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ReadwoodDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Imprime en Logcat el hash actual estático sin cambiarlo
                        NativeCryptoHelper.printFixedForCopying(
                            originalPassword = password,
                            bcryptHash = bcryptFormattedHash
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Hash actual (Toca para imprimir en Logcat sin cambiar):",
                        fontSize = 11.sp,
                        color = ReadwoodDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bcryptFormattedHash,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ReadwoodDarkTextMuted
                    )
                }
            }
        }

        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = mensajeError,
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Aceptar
        Button(
            onClick = {
                val userInput = user.trim()
                val esUsuarioValido = userInput == usuarioCorrecto
                val esContrasenaValida = NativeCryptoHelper.verify(password, hashGuardadoBD)

                // Imprimimos la versión fija para copiar al hacer click en Aceptar
                NativeCryptoHelper.printFixedForCopying(
                    originalPassword = password,
                    bcryptHash = bcryptFormattedHash
                )

                if (esUsuarioValido && esContrasenaValida) {
                    mensajeError = ""
                    onLoginExitoso()
                } else {
                    mensajeError = "El usuario o la contraseña no coinciden"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Aceptar",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "mi app",
            fontSize = 14.sp,
            color = ReadwoodDarkTextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}