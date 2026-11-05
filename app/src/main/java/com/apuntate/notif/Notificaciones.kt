package com.apuntate.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.apuntate.R

/** Utilidad para mostrar notificaciones locales de mensajes nuevos de chat. */
object Notificaciones {

    private const val CANAL_ID = "mensajes_chat"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                context.getString(R.string.notif_canal_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_canal_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    /** Comprueba si tenemos permiso para publicar notificaciones (Android 13+). */
    fun tienePermiso(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * Muestra una notificación de mensaje nuevo.
     * @param idGrupo se usa como id de la notificación para agrupar por grupo.
     */
    fun mostrarMensaje(
        context: Context,
        idGrupo: Int,
        tituloGrupo: String,
        autor: String,
        texto: String
    ) {
        if (!tienePermiso(context)) return

        val notif = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_nuevo_mensaje_en, tituloGrupo))
            .setContentText("$autor: $texto")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$autor: $texto"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(idGrupo, notif)
        } catch (_: SecurityException) {
            // Sin permiso: ignoramos silenciosamente.
        }
    }
}
