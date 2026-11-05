package com.apuntate.notif

import android.content.Context
import com.apuntate.cloud.CloudRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Observa los mensajes de todos los grupos del usuario mientras la app está activa y
 * lanza una notificación local cuando llega un mensaje nuevo de OTRA persona en un grupo
 * que no estás viendo en ese momento.
 *
 * Es una solución "en-app" (no push con la app cerrada). Para push reales haría falta
 * Firebase Cloud Messaging + Cloud Functions.
 */
class MensajesObserver(
    private val context: Context,
    private val repo: CloudRepository,
    private val uid: String,
    val scope: CoroutineScope
) {
    // Para no notificar mensajes antiguos al abrir: guardamos el último id visto por grupo.
    private val ultimoIdPorGrupo = mutableMapOf<String, String>()
    private val jobsPorGrupo = mutableMapOf<String, Job>()
    private var jobGrupos: Job? = null

    /** Id del grupo cuyo chat está abierto ahora mismo (no se notifica). */
    @Volatile
    var grupoAbiertoId: String? = null

    fun iniciar() {
        Notificaciones.crearCanal(context)
        jobGrupos = scope.launch {
            repo.observarMisGrupos(uid).collectLatest { grupos ->
                val idsActuales = grupos.map { it.id }.toSet()
                // Cancelar observadores de grupos a los que ya no pertenezco.
                (jobsPorGrupo.keys - idsActuales).forEach { id ->
                    jobsPorGrupo.remove(id)?.cancel()
                    ultimoIdPorGrupo.remove(id)
                }
                // Observar mensajes de cada grupo.
                grupos.forEach { grupo ->
                    if (jobsPorGrupo.containsKey(grupo.id)) return@forEach
                    jobsPorGrupo[grupo.id] = scope.launch {
                        repo.observarMensajes(grupo.id).collectLatest { mensajes ->
                            val ultimo = mensajes.lastOrNull() ?: return@collectLatest
                            val visto = ultimoIdPorGrupo[grupo.id]
                            // La primera vez solo registramos, sin notificar.
                            if (visto == null) {
                                ultimoIdPorGrupo[grupo.id] = ultimo.id
                                return@collectLatest
                            }
                            if (ultimo.id != visto) {
                                ultimoIdPorGrupo[grupo.id] = ultimo.id
                                val esDeOtro = ultimo.autorUid != uid
                                val noEstoyMirando = grupoAbiertoId != grupo.id
                                if (esDeOtro && noEstoyMirando) {
                                    Notificaciones.mostrarMensaje(
                                        context = context,
                                        idGrupo = grupo.id.hashCode(),
                                        tituloGrupo = grupo.nombre,
                                        autor = ultimo.autorNombre,
                                        texto = ultimo.texto
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun detener() {
        jobGrupos?.cancel()
        jobsPorGrupo.values.forEach { it.cancel() }
        jobsPorGrupo.clear()
        ultimoIdPorGrupo.clear()
    }
}
