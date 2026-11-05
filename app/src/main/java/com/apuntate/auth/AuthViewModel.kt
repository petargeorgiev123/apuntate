package com.apuntate.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Códigos de mensaje que la UI traduce al idioma activo. */
enum class AuthMensaje {
    ERROR_REGISTRO,
    ERROR_LOGIN,
    ERROR_EMAIL_RESET,
    RESET_ENVIADO,
    ERR_CREDENCIALES,
    ERR_NO_EXISTE,
    ERR_EMAIL_EN_USO,
    ERR_EMAIL_FORMATO,
    ERR_RED,
    ERR_GENERICO,
    ERR_NOMBRE
}

data class AuthUiState(
    val cargando: Boolean = false,
    val error: AuthMensaje? = null,
    val info: AuthMensaje? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    val usuario: StateFlow<UsuarioActual?> =
        repo.usuario.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repo.usuarioActual())

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun limpiarMensajes() {
        _ui.value = _ui.value.copy(error = null, info = null)
    }

    fun registrar(nombre: String, email: String, password: String) {
        if (!validar(email, password) || nombre.isBlank()) {
            _ui.value = _ui.value.copy(error = AuthMensaje.ERROR_REGISTRO)
            return
        }
        lanzar { repo.registrar(nombre, email, password) }
    }

    fun iniciarSesion(email: String, password: String) {
        if (!validar(email, password)) {
            _ui.value = _ui.value.copy(error = AuthMensaje.ERROR_LOGIN)
            return
        }
        lanzar { repo.iniciarSesion(email, password) }
    }

    fun recuperarPassword(email: String) {
        if (email.isBlank()) {
            _ui.value = _ui.value.copy(error = AuthMensaje.ERROR_EMAIL_RESET)
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            try {
                repo.enviarResetPassword(email)
                _ui.value = AuthUiState(info = AuthMensaje.RESET_ENVIADO)
            } catch (e: Exception) {
                _ui.value = AuthUiState(error = traducirError(e))
            }
        }
    }

    fun actualizarNombre(nombre: String, onHecho: () -> Unit) {
        if (nombre.isBlank()) {
            _ui.value = _ui.value.copy(error = AuthMensaje.ERR_NOMBRE)
            return
        }
        viewModelScope.launch {
            try {
                repo.actualizarNombre(nombre)
                onHecho()
            } catch (_: Exception) {
                _ui.value = _ui.value.copy(error = AuthMensaje.ERR_GENERICO)
            }
        }
    }

    fun cerrarSesion() = repo.cerrarSesion()

    private fun lanzar(bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            try {
                bloque()
                _ui.value = AuthUiState()
            } catch (e: Exception) {
                _ui.value = AuthUiState(error = traducirError(e))
            }
        }
    }

    private fun validar(email: String, password: String): Boolean =
        email.contains("@") && email.contains(".") && password.length >= 6

    private fun traducirError(e: Exception): AuthMensaje {
        val msg = e.message ?: ""
        return when {
            msg.contains("password is invalid", true) ||
                msg.contains("credential is incorrect", true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", true) -> AuthMensaje.ERR_CREDENCIALES
            msg.contains("no user record", true) -> AuthMensaje.ERR_NO_EXISTE
            msg.contains("email address is already in use", true) -> AuthMensaje.ERR_EMAIL_EN_USO
            msg.contains("badly formatted", true) -> AuthMensaje.ERR_EMAIL_FORMATO
            msg.contains("network error", true) -> AuthMensaje.ERR_RED
            else -> AuthMensaje.ERR_GENERICO
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repo) as T
    }
}
