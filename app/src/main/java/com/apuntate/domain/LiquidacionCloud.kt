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
    /** true si todos los gastos se reparten entre todos los miembros (coste por persona válido). */
    val repartoEquitativo: Boolean,
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
     * Calcula total, saldos y pagos para saldar. Trabaja en céntimos (Long).
     *
     * Cada gasto se reparte SOLO entre sus participantes (campo participantesUids).
     * Si un gasto no tiene participantes definidos, se reparte entre todos los miembros.
     * Los céntimos sobrantes de cada gasto se asignan a los primeros participantes para
     * que la suma cuadre exactamente.
     *
     * @param miembros mapa uid -> nombre de los miembros del grupo.
     * @param gastos lista de gastos del grupo.
     */
    fun calcular(miembros: Map<String, String>, gastos: List<GastoCloud>): ResumenBalanceCloud {
        if (miembros.isEmpty()) {
            return ResumenBalanceCloud(0, 0, true, emptyList(), emptyList())
        }

        val uids = miembros.keys.toList()
        val total = gastos.sumOf { it.importeCentimos }

        // Acumulado de "lo que debe cada uno" (su parte) y "lo que pagó".
        val debePorUid = HashMap<String, Long>()
        val pagoPorUid = HashMap<String, Long>()
        var repartoEquitativo = true

        for (gasto in gastos) {
            // Participantes del gasto: los indicados, o todos los miembros si no hay ninguno.
            val participantes = gasto.participantesUids
                .filter { it in miembros } // ignorar uids que ya no sean miembros
                .ifEmpty { uids }

            if (participantes.size != uids.size) repartoEquitativo = false

            // Reparto del importe del gasto entre sus participantes (céntimos exactos).
            val n = participantes.size
            if (n > 0) {
                val base = gasto.importeCentimos / n
                val resto = (gasto.importeCentimos % n).toInt()
                participantes.forEachIndexed { index, uid ->
                    val parte = base + if (index < resto) 1L else 0L
                    debePorUid[uid] = (debePorUid[uid] ?: 0L) + parte
                }
            }
            // Lo que pagó el pagador.
            pagoPorUid[gasto.pagadoPorUid] = (pagoPorUid[gasto.pagadoPorUid] ?: 0L) + gasto.importeCentimos
        }

        val saldos: List<SaldoMiembro> = uids.map { uid ->
            SaldoMiembro(
                uid = uid,
                nombre = miembros[uid] ?: "?",
                saldoCentimos = (pagoPorUid[uid] ?: 0L) - (debePorUid[uid] ?: 0L)
            )
        }

        return ResumenBalanceCloud(
            totalCentimos = total,
            porPersonaCentimos = if (uids.isNotEmpty()) total / uids.size else 0L,
            repartoEquitativo = repartoEquitativo,
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
