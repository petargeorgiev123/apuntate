package com.apuntate.ui

import java.text.NumberFormat
import java.util.Locale

/** Utilidades para formatear y parsear importes en euros usando el locale español. */
object Moneda {

    private val formatoEuro: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("es", "ES"))

    /** Convierte céntimos (Long) a un texto como "12,50 €". */
    fun formatear(centimos: Long): String = formatoEuro.format(centimos / 100.0)

    /**
     * Convierte un texto introducido por el usuario (ej. "12,50" o "12.5") a céntimos.
     * Devuelve null si no es un número válido o es negativo.
     */
    fun parsearACentimos(texto: String): Long? {
        val normalizado = texto.trim().replace(",", ".")
        if (normalizado.isEmpty()) return null
        val valor = normalizado.toDoubleOrNull() ?: return null
        if (valor < 0) return null
        return Math.round(valor * 100)
    }
}
