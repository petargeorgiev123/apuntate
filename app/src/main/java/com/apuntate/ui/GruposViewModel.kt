package com.apuntate.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apuntate.data.ApuntateRepository
import com.apuntate.data.Grupo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GruposViewModel(private val repo: ApuntateRepository) : ViewModel() {

    val grupos: StateFlow<List<Grupo>> =
        repo.grupos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun crearGrupo(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) return
        viewModelScope.launch { repo.crearGrupo(limpio) }
    }

    fun eliminarGrupo(grupo: Grupo) {
        viewModelScope.launch { repo.eliminarGrupo(grupo) }
    }

    class Factory(private val repo: ApuntateRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GruposViewModel(repo) as T
    }
}
