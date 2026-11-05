package com.apuntate.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Resultado de una acción que la UI traduce a texto según el idioma. */
enum class ResultadoGrupo {
    UNIDO_OK,
    CODIGO_NO_EXISTE,
    ERROR_CREAR,
    ERROR_UNIRSE
}

class GruposCloudViewModel(
    private val repo: CloudRepository,
    private val uid: String,
    private val nombreUsuario: String
) : ViewModel() {

    val grupos: StateFlow<List<GrupoCloud>> =
        repo.observarMisGrupos(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _resultado = MutableStateFlow<ResultadoGrupo?>(null)
    val resultado: StateFlow<ResultadoGrupo?> = _resultado.asStateFlow()

    fun limpiarResultado() { _resultado.value = null }

    fun crearGrupo(nombre: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            try {
                repo.crearGrupo(nombre, uid, nombreUsuario)
            } catch (e: Exception) {
                _resultado.value = ResultadoGrupo.ERROR_CREAR
            }
        }
    }

    fun unirseConCodigo(codigo: String) {
        if (codigo.isBlank()) return
        viewModelScope.launch {
            try {
                val id = repo.unirseConCodigo(codigo, uid, nombreUsuario)
                _resultado.value =
                    if (id != null) ResultadoGrupo.UNIDO_OK else ResultadoGrupo.CODIGO_NO_EXISTE
            } catch (e: Exception) {
                _resultado.value = ResultadoGrupo.ERROR_UNIRSE
            }
        }
    }

    class Factory(
        private val repo: CloudRepository,
        private val uid: String,
        private val nombreUsuario: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GruposCloudViewModel(repo, uid, nombreUsuario) as T
    }
}
