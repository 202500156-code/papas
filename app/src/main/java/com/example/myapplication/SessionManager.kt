package com.example.myapplication

import android.content.Context
import org.json.JSONObject

object SessionManager {
    private const val PREFS_NAME = "readwood_session_prefs"
    private const val KEY_USER_JSON = "usuario_autenticado_json"
    private const val KEY_DARK_MODE = "modo_oscuro_permanente"

    fun guardarModoOscuro(context: Context, activo: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, activo).apply()
    }

    fun obtenerModoOscuro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun guardarSesion(context: Context, usuario: UsuarioPerfil) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("id", usuario.id)
            put("usuario", usuario.usuario)
            put("correo", usuario.correo)
            put("passwordHash", usuario.passwordHash)
            put("salt", usuario.salt)
            put("fotoBase64", usuario.fotoBase64)
            put("consintioFoto", usuario.consintioFoto)
            put("redInstagram", usuario.redInstagram)
            put("redTwitter", usuario.redTwitter)
            put("consintioRedes", usuario.consintioRedes)
            put("ocultarSeguidos", usuario.ocultarSeguidos)
        }
        prefs.edit().putString(KEY_USER_JSON, json.toString()).apply()
    }

    fun obtenerSesionGuardada(context: Context): UsuarioPerfil? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            val obj = JSONObject(raw)
            UsuarioPerfil(
                id = obj.optString("id"),
                usuario = obj.optString("usuario"),
                correo = obj.optString("correo"),
                passwordHash = obj.optString("passwordHash"),
                salt = obj.optString("salt"),
                fotoBase64 = obj.optString("fotoBase64"),
                consintioFoto = obj.optBoolean("consintioFoto", false),
                redInstagram = obj.optString("redInstagram"),
                redTwitter = obj.optString("redTwitter"),
                consintioRedes = obj.optBoolean("consintioRedes", false),
                ocultarSeguidos = obj.optBoolean("ocultarSeguidos", false)
            ).takeIf { it.id.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    fun cerrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_JSON).apply()
    }
}