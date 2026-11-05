package com.example.myapplication

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom

object NativeCryptoHelper {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun generateBcryptStyleHash(text: String, salt: String): String {
        if (text.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest((text + salt).toByteArray())
        val base64Like = bytes.joinToString("") { "%02x".format(it) }.take(31)
        val salt22 = salt.take(22)
        return "\$2b\$10\$$salt22$base64Like"
    }

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

data class CuentaUsuario(
    val usuario: String,
    val contrasena: String
)

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit
) {
    val context = LocalContext.current
    var usuarioIngresado by remember { mutableStateOf("") }
    var passwordIngresada by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    var saltEstatico by remember { mutableStateOf(NativeCryptoHelper.generateSalt()) }
    var bcryptFormattedHash by remember { mutableStateOf("") }

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
            painter = painterResource(id = R.drawable.logoapp),
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

        // Campo para ingresar Usuario
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("user:", fontSize = 14.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.width(80.dp))
            OutlinedTextField(
                value = usuarioIngresado,
                onValueChange = {
                    usuarioIngresado = it
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

        Spacer(modifier = Modifier.height(8.dp))

        // Opción para registrar/agregar cuenta en el JSON
        Text(
            text = "+ Agregar cuenta",
            color = ReadwoodDarkPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val userClean = usuarioIngresado.trim()
                    val passClean = passwordIngresada.trim()

                    if (userClean.isNotEmpty() && passClean.isNotEmpty()) {
                        guardarPerfilEnJson(context, CuentaUsuario(userClean, passClean))
                        mensajeError = "¡Cuenta guardada exitosamente en el JSON!"
                    } else {
                        mensajeError = "Ingresa usuario y contraseña antes de guardar"
                    }
                }
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para ingresar Contraseña
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("password:", fontSize = 14.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.width(80.dp))
            OutlinedTextField(
                value = passwordIngresada,
                onValueChange = {
                    passwordIngresada = it
                    mensajeError = ""
                    bcryptFormattedHash = NativeCryptoHelper.generateBcryptStyleHash(it, saltEstatico)
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

        if (bcryptFormattedHash.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ReadwoodDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        NativeCryptoHelper.printFixedForCopying(
                            originalPassword = passwordIngresada,
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
                color = if (mensajeError.contains("exitosamente")) ReadwoodDarkPrimary else Color(0xFFFF6B6B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userClean = usuarioIngresado.trim()
                val passClean = passwordIngresada.trim()

                NativeCryptoHelper.printFixedForCopying(
                    originalPassword = passwordIngresada,
                    bcryptHash = bcryptFormattedHash
                )

                // Cargar cuentas guardadas en el JSON
                val cuentasGuardadas = cargarCuentasGuardadas(context)

                // Verificar si coinciden con alguna registrada en el JSON
                val existeEnJson = cuentasGuardadas.any { it.usuario == userClean && it.contrasena == passClean }

                if (existeEnJson) {
                    mensajeError = ""
                    onLoginExitoso()
                } else {
                    mensajeError = "La cuenta o contraseña no coinciden con las guardadas"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Aceptar", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
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

private fun guardarPerfilEnJson(context: Context, nuevoPerfil: CuentaUsuario) {
    try {
        val file = File(context.filesDir, "perfil.json")
        val jsonArray = if (file.exists()) {
            val contenido = file.readText()
            if (contenido.startsWith("[")) JSONArray(contenido) else JSONArray()
        } else {
            JSONArray()
        }

        var encontrada = false
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            if (item.getString("usuario") == nuevoPerfil.usuario) {
                item.put("contrasena", nuevoPerfil.contrasena)
                encontrada = true
                break
            }
        }

        if (!encontrada) {
            val nuevoObj = JSONObject().apply {
                put("usuario", nuevoPerfil.usuario)
                put("contrasena", nuevoPerfil.contrasena)
            }
            jsonArray.put(nuevoObj)
        }

        val jsonString = jsonArray.toString(4)
        file.writeText(jsonString)

        Log.d("ReadwoodJson", "JSON actualizado exitosamente:\n$jsonString")
    } catch (e: Exception) {
        Log.e("ReadwoodJson", "Error al guardar en JSON: ${e.message}")
    }
}

private fun cargarCuentasGuardadas(context: Context): List<CuentaUsuario> {
    val lista = mutableListOf<CuentaUsuario>()
    try {
        val file = File(context.filesDir, "perfil.json")
        if (file.exists()) {
            val jsonArray = JSONArray(file.readText())
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(CuentaUsuario(obj.getString("usuario"), obj.getString("contrasena")))
            }
        }
    } catch (e: Exception) {
        Log.e("ReadwoodJson", "Error al cargar JSON: ${e.message}")
    }
    return lista
}