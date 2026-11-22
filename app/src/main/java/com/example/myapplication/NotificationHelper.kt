package com.example.myapplication

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID = "readwood_alertas_channel"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de Readwood",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de seguidores, comentarios y novedades"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun estaSilenciado(context: Context): Boolean {
        val prefs = context.getSharedPreferences("readwood_notifs_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("silenciar_todo", false)
    }

    fun setSilenciado(context: Context, silenciar: Boolean) {
        val prefs = context.getSharedPreferences("readwood_notifs_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("silenciar_todo", silenciar).apply()
    }

    fun mostrarNotificacionSistema(context: Context, id: Int, titulo: String, mensaje: String) {
        if (estaSilenciado(context)) return
        crearCanal(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permiso != PackageManager.PERMISSION_GRANTED) return
        }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notif)
    }
}