package com.apuntate.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Enlaces legales públicos de la app (exigidos por Google Play). */
object EnlacesLegales {
    const val POLITICA_PRIVACIDAD =
        "https://github.com/petargeorgiev123/apuntate/blob/main/PRIVACY.md"

    fun abrir(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            // Sin navegador instalado: no hacemos nada.
        }
    }
}
