package com.apuntate.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Un grupo representa una actividad/viaje (ej. "Esquí en Sierra Nevada"). */
@Entity(tableName = "grupos")
data class Grupo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

/** Una persona que participa en un grupo. */
@Entity(
    tableName = "participantes",
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("grupoId")]
)
data class Participante(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grupoId: Long,
    val nombre: String
)

/** Un gasto pagado por un participante dentro de un grupo. */
@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Participante::class,
            parentColumns = ["id"],
            childColumns = ["pagadoPorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("grupoId"), Index("pagadoPorId")]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grupoId: Long,
    val concepto: String,
    val importeCentimos: Long,
    val pagadoPorId: Long
)
