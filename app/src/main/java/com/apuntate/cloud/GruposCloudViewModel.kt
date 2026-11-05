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

class GruposCloudViewModel(
    private val repo: CloudRepository,
    private val uid: String,
    private val nombreUsuario: String
) : ViewModel() {

    val grupos: StateFlow<List<GrupoCloud>> =
        repo.observarMisGrupos(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun limpiarMensaje() { _mensaje.value = null }

    fun crearGrupo(nombre: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            try {
                repo.crearGrupo(nombre, uid, nombreUsuario)
            } catch (e: Exception) {
                _mensaje.value = "No se pudo crear el grupo. Revisa tu conexión."
            }
        }
    }

    fun unirseConCodigo(codigo: String) {
        if (codigo.isBlank()) return
        viewModelScope.launch {
            try {
                val id = repo.unirseConCodigo(codigo, uid, nombreUsuario)
                _mensaje.value = if (id != null) "¡Te has unido al grupo!"
                else "No existe ningún grupo con ese código."
            } catch (e: Exception) {
                _mensaje.value = "No se pudo unir al grupo. Revisa tu conexión."
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
