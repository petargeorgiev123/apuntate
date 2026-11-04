package com.apuntate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApuntateDao {

    // ---- Grupos ----
    @Query("SELECT * FROM grupos ORDER BY id DESC")
    fun observarGrupos(): Flow<List<Grupo>>

    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    fun observarGrupo(grupoId: Long): Flow<Grupo?>

    @Insert
    suspend fun insertarGrupo(grupo: Grupo): Long

    @Update
    suspend fun actualizarGrupo(grupo: Grupo)

    @Delete
    suspend fun eliminarGrupo(grupo: Grupo)

    // ---- Participantes ----
    @Query("SELECT * FROM participantes WHERE grupoId = :grupoId ORDER BY nombre COLLATE NOCASE")
    fun observarParticipantes(grupoId: Long): Flow<List<Participante>>

    @Insert
    suspend fun insertarParticipante(participante: Participante): Long

    @Delete
    suspend fun eliminarParticipante(participante: Participante)

    // ---- Gastos ----
    @Query("SELECT * FROM gastos WHERE grupoId = :grupoId ORDER BY id DESC")
    fun observarGastos(grupoId: Long): Flow<List<Gasto>>

    @Insert
    suspend fun insertarGasto(gasto: Gasto): Long

    @Delete
    suspend fun eliminarGasto(gasto: Gasto)
}
