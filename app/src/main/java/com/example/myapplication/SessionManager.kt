package com.example.myapplication

import android.content.Context
import org.json.JSONArray

object SessionManager {
    private const val PREFS_NAME = "readwood_session_prefs"
    private const val KEY_DARK_MODE = "modo_oscuro"

    fun guardarSesion(context: Context, usuario: UsuarioPerfil) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("id", usuario.id)
            .putString("usuario", usuario.usuario)
            .putString("correo", usuario.correo)
            .putBoolean("mostrarCorreoPublico", usuario.mostrarCorreoPublico)
            .putString("fotoBase64", usuario.fotoBase64)
            .putBoolean("consintioFoto", usuario.consintioFoto)
            .putString("bannerBase64", usuario.bannerBase64)
            .putString("bannerColorHex", usuario.bannerColorHex)
            .putString("enlacesRedes", JSONArray(usuario.enlacesRedes).toString())
            .apply()
    }

    fun obtenerSesion(context: Context): UsuarioPerfil? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString("id", null) ?: return null
        val arrRedes = JSONArray(prefs.getString("enlacesRedes", "[]") ?: "[]")
        val listaRedes = List(arrRedes.length()) { i -> arrRedes.optString(i) }
        return UsuarioPerfil(
            id = id,
            usuario = prefs.getString("usuario", "") ?: "",
            correo = prefs.getString("correo", "") ?: "",
            mostrarCorreoPublico = prefs.getBoolean("mostrarCorreoPublico", false),
            fotoBase64 = prefs.getString("fotoBase64", "") ?: "",
            consintioFoto = prefs.getBoolean("consintioFoto", false),
            bannerBase64 = prefs.getString("bannerBase64", "") ?: "",
            bannerColorHex = prefs.getString("bannerColorHex", "#5E2D40") ?: "#5E2D40",
            enlacesRedes = listaRedes
        )
    }

    fun cerrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun guardarModoOscuro(context: Context, oscuro: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, oscuro).apply()
    }

    fun obtenerModoOscuro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
}