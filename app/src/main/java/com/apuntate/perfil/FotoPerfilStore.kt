package com.apuntate.perfil

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Guarda y recupera la foto de perfil del usuario en el almacenamiento interno de la app.
 * La foto es local a este dispositivo (no se comparte con otros miembros).
 */
object FotoPerfilStore {

    private const val NOMBRE_FICHERO = "foto_perfil.jpg"

    private fun fichero(context: Context): File =
        File(context.filesDir, NOMBRE_FICHERO)

    /** Copia la imagen seleccionada (uri de la galería) al almacenamiento interno. */
    fun guardarDesdeUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                fichero(context).outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            } != null
        } catch (e: Exception) {
            false
        }
    }

    /** Devuelve el fichero de la foto si existe, o null. */
    fun obtener(context: Context): File? =
        fichero(context).takeIf { it.exists() && it.length() > 0 }

    fun eliminar(context: Context) {
        fichero(context).delete()
    }
}
