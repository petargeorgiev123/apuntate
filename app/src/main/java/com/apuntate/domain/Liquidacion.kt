package com.apuntate.domain

import com.apuntate.data.Gasto
import com.apuntate.data.Participante

/** Representa una transferencia sugerida para saldar cuentas: [de] paga [cantidadCentimos] a [a]. */
data class Pago(
    val de: String,
    val a: String,
    val cantidadCentimos: Long
)

/** Resultado del cálculo del balance de un grupo. */
data class ResumenBalance(
    val totalCentimos: Long,
    val porPersonaCentimos: Long,
    /** Saldo neto por participante (positivo = le deben, negativo = debe). */
    val saldos: Map<Participante, Long>,
    /** Lista de pagos sugeridos para dejar todo a cero. */
    val pagos: List<Pago>
)

object CalculadoraLiquidacion {

    /**
     * Calcula el total, el coste por persona, el saldo de cada participante y la lista
     * mínima de pagos para saldar las cuentas.
     *
     * Todo se trabaja en céntimos (Long) para evitar errores de redondeo con decimales.
     * El reparto del coste por persona distribuye los céntimos sobrantes entre los
     * primeros participantes para que la suma cuadre exactamente.
     */
    fun calcular(participantes: List<Participante>, gastos: List<Gasto>): ResumenBalance {
        if (participantes.isEmpty()) {
            return ResumenBalance(0, 0, emptyMap(), emptyList())
        }

        val total = gastos.sumOf { it.importeCentimos }
        val n = participantes.size

        // Reparto justo de céntimos: cada uno paga base; los primeros 'resto' pagan 1 céntimo más.
        val base = total / n
        val resto = (total % n).toInt()
        val cuotaPorParticipante: Map<Long, Long> = participantes.mapIndexed { index, p ->
            p.id to (base + if (index < resto) 1L else 0L)
        }.toMap()

        // Cuánto ha pagado realmente cada participante.
        val pagadoPorId: Map<Long, Long> = gastos
            .groupBy { it.pagadoPorId }
            .mapValues { entry -> entry.value.sumOf { it.importeCentimos } }

        // Saldo = pagado - cuota que le corresponde. Positivo: le deben. Negativo: debe.
        val saldos: Map<Participante, Long> = participantes.associateWith { p ->
            (pagadoPorId[p.id] ?: 0L) - (cuotaPorParticipante[p.id] ?: 0L)
        }

        val pagos = calcularPagos(saldos)

        return ResumenBalance(
            totalCentimos = total,
            porPersonaCentimos = if (n > 0) total / n else 0L,
            saldos = saldos,
            pagos = pagos
        )
    }

    /**
     * Minimiza los pagos emparejando a quien más debe con quien más le deben,
     * de forma iterativa (algoritmo voraz habitual para "settle up").
     */
    private fun calcularPagos(saldos: Map<Participante, Long>): List<Pago> {
        // Trabajamos sobre copias mutables de (nombre, saldo).
        val deudores = saldos.filter { it.value < 0 }
            .map { it.key.nombre to it.value }
            .toMutableList()
        val acreedores = saldos.filter { it.value > 0 }
            .map { it.key.nombre to it.value }
            .toMutableList()

        val pagos = mutableListOf<Pago>()

        var i = 0
        var j = 0
        while (i < deudores.size && j < acreedores.size) {
            val (nombreDeudor, deuda) = deudores[i]      // deuda es negativa
            val (nombreAcreedor, credito) = acreedores[j] // credito es positivo

            val cantidad = minOf(-deuda, credito)
            if (cantidad > 0) {
                pagos.add(Pago(de = nombreDeudor, a = nombreAcreedor, cantidadCentimos = cantidad))
            }

            val nuevaDeuda = deuda + cantidad
            val nuevoCredito = credito - cantidad
            deudores[i] = nombreDeudor to nuevaDeuda
            acreedores[j] = nombreAcreedor to nuevoCredito

            if (nuevaDeuda == 0L) i++
            if (nuevoCredito == 0L) j++
        }

        return pagos
    }
}
