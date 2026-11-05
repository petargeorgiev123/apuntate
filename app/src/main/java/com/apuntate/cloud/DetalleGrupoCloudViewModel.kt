package com.apuntate.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apuntate.domain.CalculadoraLiquidacionCloud
import com.apuntate.domain.ResumenBalanceCloud
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleGrupoCloudUiState(
    val grupo: GrupoCloud? = null,
    val gastos: List<GastoCloud> = emptyList(),
    val mensajes: List<MensajeCloud> = emptyList(),
    val balance: ResumenBalanceCloud = ResumenBalanceCloud(0, 0, emptyList(), emptyList())
)

class DetalleGrupoCloudViewModel(
    private val repo: CloudRepository,
    private val grupoId: String,
    val uid: String,
    val nombreUsuario: String
) : ViewModel() {

    val uiState: StateFlow<DetalleGrupoCloudUiState> = combine(
        repo.observarGrupo(grupoId),
        repo.observarGastos(grupoId),
        repo.observarMensajes(grupoId)
    ) { grupo, gastos, mensajes ->
        DetalleGrupoCloudUiState(
            grupo = grupo,
            gastos = gastos,
            mensajes = mensajes,
            balance = CalculadoraLiquidacionCloud.calcular(grupo?.miembros ?: emptyMap(), gastos)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalleGrupoCloudUiState())

    init {
        // Al abrir el grupo, corrige el nombre guardado (p. ej. si antes quedó el email).
        viewModelScope.launch {
            try {
                repo.sincronizarMiNombre(grupoId, uid, nombreUsuario)
            } catch (_: Exception) {
                // Silencioso: si falla (p. ej. sin conexión), no bloquea la pantalla.
            }
        }
    }

    fun añadirGasto(concepto: String, importeCentimos: Long, pagadoPorUid: String, pagadoPorNombre: String) {
        if (concepto.isBlank() || importeCentimos <= 0) return
        viewModelScope.launch {
            repo.añadirGasto(grupoId, concepto, importeCentimos, pagadoPorUid, pagadoPorNombre)
        }
    }

    fun eliminarGasto(gastoId: String) {
        viewModelScope.launch { repo.eliminarGasto(grupoId, gastoId) }
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            repo.enviarMensaje(grupoId, texto, uid, nombreUsuario)
        }
    }

    fun salirDelGrupo(onHecho: () -> Unit) {
        viewModelScope.launch {
            repo.salirDelGrupo(grupoId, uid)
            onHecho()
        }
    }

    class Factory(
        private val repo: CloudRepository,
        private val grupoId: String,
        private val uid: String,
        private val nombreUsuario: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetalleGrupoCloudViewModel(repo, grupoId, uid, nombreUsuario) as T
    }
}
