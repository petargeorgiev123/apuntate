package com.apuntate.domain

import com.apuntate.cloud.GastoCloud

/** Representa una transferencia sugerida para saldar cuentas: [de] paga [cantidadCentimos] a [a]. */
data class Pago(
    val de: String,
    val a: String,
    val cantidadCentimos: Long
)

/** Resultado del cálculo del balance de un grupo en la nube. */
data class ResumenBalanceCloud(
    val totalCentimos: Long,
    val porPersonaCentimos: Long,
    /** Saldo neto por nombre de miembro (positivo = le deben, negativo = debe). */
    val saldos: List<SaldoMiembro>,
    val pagos: List<Pago>
)

data class SaldoMiembro(
    val uid: String,
    val nombre: String,
    val saldoCentimos: Long
)

object CalculadoraLiquidacionCloud {

    /**
     * Calcula total, coste por persona, saldos y pagos para saldar, usando el mismo
     * algoritmo probado de la versión local. Trabaja en céntimos (Long).
     *
     * @param miembros mapa uid -> nombre de los miembros del grupo.
     * @param gastos lista de gastos del grupo.
     */
    fun calcular(miembros: Map<String, String>, gastos: List<GastoCloud>): ResumenBalanceCloud {
        if (miembros.isEmpty()) {
            return ResumenBalanceCloud(0, 0, emptyList(), emptyList())
        }

        val total = gastos.sumOf { it.importeCentimos }
        val uids = miembros.keys.toList()
        val n = uids.size

        // Reparto justo de céntimos: los primeros 'resto' pagan 1 céntimo más.
        val base = total / n
        val resto = (total % n).toInt()
        val cuotaPorUid: Map<String, Long> = uids.mapIndexed { index, uid ->
            uid to (base + if (index < resto) 1L else 0L)
        }.toMap()

        val pagadoPorUid: Map<String, Long> = gastos
            .groupBy { it.pagadoPorUid }
            .mapValues { entry -> entry.value.sumOf { it.importeCentimos } }

        val saldos: List<SaldoMiembro> = uids.map { uid ->
            SaldoMiembro(
                uid = uid,
                nombre = miembros[uid] ?: "?",
                saldoCentimos = (pagadoPorUid[uid] ?: 0L) - (cuotaPorUid[uid] ?: 0L)
            )
        }

        return ResumenBalanceCloud(
            totalCentimos = total,
            porPersonaCentimos = total / n,
            saldos = saldos,
            pagos = calcularPagos(saldos)
        )
    }

    private fun calcularPagos(saldos: List<SaldoMiembro>): List<Pago> {
        val deudores = saldos.filter { it.saldoCentimos < 0 }
            .map { it.nombre to it.saldoCentimos }
            .toMutableList()
        val acreedores = saldos.filter { it.saldoCentimos > 0 }
            .map { it.nombre to it.saldoCentimos }
            .toMutableList()

        val pagos = mutableListOf<Pago>()
        var i = 0
        var j = 0
        while (i < deudores.size && j < acreedores.size) {
            val (nombreDeudor, deuda) = deudores[i]
            val (nombreAcreedor, credito) = acreedores[j]
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
