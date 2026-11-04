package com.apuntate.data

import kotlinx.coroutines.flow.Flow

class ApuntateRepository(private val dao: ApuntateDao) {

    fun grupos(): Flow<List<Grupo>> = dao.observarGrupos()
    fun grupo(grupoId: Long): Flow<Grupo?> = dao.observarGrupo(grupoId)
    fun participantes(grupoId: Long): Flow<List<Participante>> = dao.observarParticipantes(grupoId)
    fun gastos(grupoId: Long): Flow<List<Gasto>> = dao.observarGastos(grupoId)

    suspend fun crearGrupo(nombre: String): Long = dao.insertarGrupo(Grupo(nombre = nombre))
    suspend fun eliminarGrupo(grupo: Grupo) = dao.eliminarGrupo(grupo)

    suspend fun añadirParticipante(grupoId: Long, nombre: String) =
        dao.insertarParticipante(Participante(grupoId = grupoId, nombre = nombre))

    suspend fun eliminarParticipante(participante: Participante) =
        dao.eliminarParticipante(participante)

    suspend fun añadirGasto(grupoId: Long, concepto: String, importeCentimos: Long, pagadoPorId: Long) =
        dao.insertarGasto(
            Gasto(
                grupoId = grupoId,
                concepto = concepto,
                importeCentimos = importeCentimos,
                pagadoPorId = pagadoPorId
            )
        )

    suspend fun eliminarGasto(gasto: Gasto) = dao.eliminarGasto(gasto)
}
