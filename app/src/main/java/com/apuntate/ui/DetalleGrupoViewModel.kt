package com.apuntate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apuntate.data.ApuntateRepository
import com.apuntate.data.Gasto
import com.apuntate.data.Grupo
import com.apuntate.data.Participante
import com.apuntate.domain.CalculadoraLiquidacion
import com.apuntate.domain.ResumenBalance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleGrupoUiState(
    val grupo: Grupo? = null,
    val participantes: List<Participante> = emptyList(),
    val gastos: List<Gasto> = emptyList(),
    val balance: ResumenBalance = ResumenBalance(0, 0, emptyMap(), emptyList())
)

class DetalleGrupoViewModel(
    private val repo: ApuntateRepository,
    private val grupoId: Long
) : ViewModel() {

    val uiState: StateFlow<DetalleGrupoUiState> = combine(
        repo.grupo(grupoId),
        repo.participantes(grupoId),
        repo.gastos(grupoId)
    ) { grupo, participantes, gastos ->
        DetalleGrupoUiState(
            grupo = grupo,
            participantes = participantes,
            gastos = gastos,
            balance = CalculadoraLiquidacion.calcular(participantes, gastos)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalleGrupoUiState())

    fun añadirParticipante(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { repo.añadirParticipante(grupoId, limpio) }
    }

    fun eliminarParticipante(participante: Participante) {
        viewModelScope.launch { repo.eliminarParticipante(participante) }
    }

    fun añadirGasto(concepto: String, importeCentimos: Long, pagadoPorId: Long) {
        val limpio = concepto.trim()
        if (limpio.isEmpty() || importeCentimos <= 0) return
        viewModelScope.launch { repo.añadirGasto(grupoId, limpio, importeCentimos, pagadoPorId) }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch { repo.eliminarGasto(gasto) }
    }

    class Factory(
        private val repo: ApuntateRepository,
        private val grupoId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetalleGrupoViewModel(repo, grupoId) as T
    }
}
