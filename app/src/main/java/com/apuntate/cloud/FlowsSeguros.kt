package com.apuntate.cloud

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen

/**
 * Hace que un listener de Firestore nunca cierre la app por un error:
 * reintenta unas cuantas veces con espera creciente (p. ej. un permiso denegado justo
 * después de crear un grupo, antes de que el servidor lo haya guardado) y, si sigue
 * fallando, deja de emitir y registra el error en lugar de lanzarlo.
 */
fun <T> Flow<T>.sinCaidas(etiqueta: String): Flow<T> = this
    .retryWhen { causa, intento ->
        Log.w("Apuntate", "$etiqueta: error en listener (intento ${intento + 1}): ${causa.message}")
        if (intento < 5) {
            delay(1500L * (intento + 1))
            true
        } else {
            false
        }
    }
    .catch { Log.e("Apuntate", "$etiqueta: listener abandonado", it) }
