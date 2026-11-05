package com.apuntate.cloud

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Grupo almacenado en Firestore. Todos los miembros ven el mismo documento en tiempo real.
 *
 * Los importes se guardan en céntimos (Long) para evitar errores de redondeo.
 */
data class GrupoCloud(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val creadoPor: String = "",
    /** UIDs de los miembros; se usa para consultar "mis grupos" con whereArrayContains. */
    val miembrosUids: List<String> = emptyList(),
    /** Mapa uid -> nombre visible del miembro. */
    val miembros: Map<String, String> = emptyMap(),
    /** Código corto para invitar a otras personas al grupo. */
    val codigoInvitacion: String = "",
    val creadoEn: Timestamp? = null
)

/** Gasto dentro de un grupo (subcolección grupos/{id}/gastos). */
data class GastoCloud(
    @DocumentId val id: String = "",
    val concepto: String = "",
    val importeCentimos: Long = 0,
    /** UID del miembro que pagó. */
    val pagadoPorUid: String = "",
    /** Nombre del pagador en el momento del gasto (para mostrar sin otra consulta). */
    val pagadoPorNombre: String = "",
    val creadoEn: Timestamp? = null
)

/** Mensaje de chat dentro de un grupo (subcolección grupos/{id}/mensajes). */
data class MensajeCloud(
    @DocumentId val id: String = "",
    val texto: String = "",
    val autorUid: String = "",
    val autorNombre: String = "",
    val enviadoEn: Timestamp? = null
)
