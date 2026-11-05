package com.apuntate.cloud

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Acceso a los datos en la nube (Firestore): grupos, gastos y mensajes de chat.
 * Expone listeners en tiempo real como Flows.
 */
class CloudRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val grupos = db.collection("grupos")

    // ---------------- Grupos ----------------

    /** Escucha en tiempo real los grupos a los que pertenece el usuario. */
    fun observarMisGrupos(uid: String): Flow<List<GrupoCloud>> = callbackFlow {
        val registro = grupos
            .whereArrayContains("miembrosUids", uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val lista = snap?.toObjects(GrupoCloud::class.java).orEmpty()
                    .sortedByDescending { it.creadoEn?.seconds ?: 0 }
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    /** Escucha en tiempo real un grupo concreto. */
    fun observarGrupo(grupoId: String): Flow<GrupoCloud?> = callbackFlow {
        val registro = grupos.document(grupoId).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snap?.toObject(GrupoCloud::class.java))
        }
        awaitClose { registro.remove() }
    }

    suspend fun crearGrupo(nombre: String, uid: String, nombreUsuario: String): String {
        val doc = grupos.document()
        val grupo = GrupoCloud(
            id = doc.id,
            nombre = nombre.trim(),
            creadoPor = uid,
            miembrosUids = listOf(uid),
            miembros = mapOf(uid to nombreUsuario),
            codigoInvitacion = generarCodigo()
        )
        // Usamos set con FieldValue.serverTimestamp para creadoEn.
        val datos = hashMapOf(
            "nombre" to grupo.nombre,
            "creadoPor" to grupo.creadoPor,
            "miembrosUids" to grupo.miembrosUids,
            "miembros" to grupo.miembros,
            "codigoInvitacion" to grupo.codigoInvitacion,
            "creadoEn" to FieldValue.serverTimestamp()
        )
        doc.set(datos).await()
        return doc.id
    }

    /** Une al usuario al grupo cuyo código coincide. Devuelve el id del grupo o null si no existe. */
    suspend fun unirseConCodigo(codigo: String, uid: String, nombreUsuario: String): String? {
        val resultado = grupos
            .whereEqualTo("codigoInvitacion", codigo.trim().uppercase())
            .limit(1)
            .get()
            .await()
        val doc = resultado.documents.firstOrNull() ?: return null
        doc.reference.update(
            mapOf(
                "miembrosUids" to FieldValue.arrayUnion(uid),
                "miembros.$uid" to nombreUsuario
            )
        ).await()
        return doc.id
    }

    /** Sale del grupo. Si era el último miembro, borra el grupo. */
    suspend fun salirDelGrupo(grupoId: String, uid: String) {
        val ref = grupos.document(grupoId)
        val snap = ref.get().await()
        val grupo = snap.toObject(GrupoCloud::class.java)
        if (grupo != null && grupo.miembrosUids.size <= 1) {
            ref.delete().await()
        } else {
            ref.update(
                mapOf(
                    "miembrosUids" to FieldValue.arrayRemove(uid),
                    "miembros.$uid" to FieldValue.delete()
                )
            ).await()
        }
    }

    // ---------------- Gastos ----------------

    fun observarGastos(grupoId: String): Flow<List<GastoCloud>> = callbackFlow {
        val registro = grupos.document(grupoId).collection("gastos")
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snap?.toObjects(GastoCloud::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun añadirGasto(
        grupoId: String,
        concepto: String,
        importeCentimos: Long,
        pagadoPorUid: String,
        pagadoPorNombre: String
    ) {
        val datos = hashMapOf(
            "concepto" to concepto.trim(),
            "importeCentimos" to importeCentimos,
            "pagadoPorUid" to pagadoPorUid,
            "pagadoPorNombre" to pagadoPorNombre,
            "creadoEn" to FieldValue.serverTimestamp()
        )
        grupos.document(grupoId).collection("gastos").add(datos).await()
    }

    suspend fun eliminarGasto(grupoId: String, gastoId: String) {
        grupos.document(grupoId).collection("gastos").document(gastoId).delete().await()
    }

    /**
     * Sincroniza el nombre del usuario en este grupo con su nombre actual de perfil.
     * Corrige casos antiguos en los que se guardó el email en lugar del nombre:
     *  - Actualiza su entrada en el mapa de miembros.
     *  - Actualiza el nombre del pagador en sus gastos ya registrados.
     * Solo escribe si detecta que el nombre guardado es distinto del actual.
     */
    suspend fun sincronizarMiNombre(grupoId: String, uid: String, nombreActual: String) {
        if (nombreActual.isBlank()) return
        val ref = grupos.document(grupoId)
        val snap = ref.get().await()
        val grupo = snap.toObject(GrupoCloud::class.java) ?: return

        val nombreGuardado = grupo.miembros[uid]
        if (nombreGuardado == nombreActual) return // ya está correcto, no hacemos nada

        // 1) Actualizar el nombre del miembro en el grupo.
        ref.update("miembros.$uid", nombreActual).await()

        // 2) Actualizar el nombre del pagador en sus gastos antiguos.
        val gastosDelUsuario = ref.collection("gastos")
            .whereEqualTo("pagadoPorUid", uid)
            .get()
            .await()
        for (gasto in gastosDelUsuario.documents) {
            if (gasto.getString("pagadoPorNombre") != nombreActual) {
                gasto.reference.update("pagadoPorNombre", nombreActual).await()
            }
        }
    }

    // ---------------- Chat ----------------

    fun observarMensajes(grupoId: String): Flow<List<MensajeCloud>> = callbackFlow {
        val registro = grupos.document(grupoId).collection("mensajes")
            .orderBy("enviadoEn", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snap?.toObjects(MensajeCloud::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun enviarMensaje(grupoId: String, texto: String, autorUid: String, autorNombre: String) {
        val datos = hashMapOf(
            "texto" to texto.trim(),
            "autorUid" to autorUid,
            "autorNombre" to autorNombre,
            "enviadoEn" to FieldValue.serverTimestamp()
        )
        grupos.document(grupoId).collection("mensajes").add(datos).await()
    }

    private fun generarCodigo(longitud: Int = 6): String {
        val letras = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // sin caracteres ambiguos
        return (1..longitud).map { letras[Random.nextInt(letras.length)] }.joinToString("")
    }
}
