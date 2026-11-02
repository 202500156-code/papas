package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

// ESTE IMPORT RESUELVE LOS ERRORES
import com.example.myapplication.ui.theme.*

import org.json.JSONObject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    val context = LocalContext.current

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
            painter = painterResource(id = R.drawable.mi_imagen),
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

        Text("user:", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = user,
            onValueChange = {
                user = it
                mensajeError = ""
            },
            placeholder = { Text("aqui el usuario", color = ReadwoodDarkTextMuted) },
            singleLine = true,
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

        Text("password:", fontSize = 12.sp, color = ReadwoodDarkTextMuted, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                mensajeError = ""
            },
            placeholder = { Text("aqui mete la contrasena", color = ReadwoodDarkTextMuted) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

        Button(
            onClick = {
                if (user.trim().isNotEmpty() && password.isNotEmpty()) {
                    mensajeError = ""

                    val perfil = UsuarioPerfil(
                        usuario = user.trim(),
                        contrasena = password
                    )

                    guardarPerfilJson(context, perfil)
                    onLoginExitoso()
                } else {
                    mensajeError = "Por favor ingresa usuario y contraseña"
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

        Text("mi app", fontSize = 14.sp, color = ReadwoodDarkTextMuted, fontWeight = FontWeight.Medium)
    }
}

private fun guardarPerfilJson(context: Context, perfil: UsuarioPerfil) {
    try {
        val jsonObject = JSONObject().apply {
            put("usuario", perfil.usuario)
            put("contrasena", perfil.contrasena)
        }

        val jsonString = jsonObject.toString(4)

        Log.d("ReadwoodJson", "JSON Generado:\n$jsonString")

        val file = File(context.filesDir, "perfil.json")
        file.writeText(jsonString)

        Log.d("ReadwoodJson", "Archivo guardado en: ${file.absolutePath}")
    } catch (e: Exception) {
        Log.e("ReadwoodJson", "Error al crear JSON: ${e.message}")
    }
}