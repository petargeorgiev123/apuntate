package com.apuntate.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Idiomas soportados por la app. */
enum class Idioma(val codigo: String) {
    ESPANOL("es"),
    INGLES("en"),
    BULGARO("bg");

    companion object {
        fun desdeCodigo(codigo: String?): Idioma =
            entries.firstOrNull { codigo != null && codigo.startsWith(it.codigo) } ?: ESPANOL
    }
}

/**
 * Gestiona el idioma de la app usando el mecanismo per-app language de AndroidX.
 * El idioma elegido se guarda y restaura automáticamente por el sistema.
 */
object LocaleManager {

    /** Idioma actualmente aplicado (según la configuración per-app). */
    fun idiomaActual(): Idioma {
        val locales = AppCompatDelegate.getApplicationLocales()
        val codigo = if (!locales.isEmpty) locales[0]?.language else null
        return Idioma.desdeCodigo(codigo)
    }

    /** Aplica el idioma elegido. El cambio recrea las actividades automáticamente. */
    fun aplicar(idioma: Idioma) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(idioma.codigo)
        )
    }
}
