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

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    /** Estado de sesión: null = sin sesión. Lo observa la navegación para decidir la pantalla. */
    val usuario: StateFlow<UsuarioActual?> =
        repo.usuario.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repo.usuarioActual())

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun limpiarMensajes() {
        _ui.value = _ui.value.copy(error = null, mensaje = null)
    }

    fun registrar(nombre: String, email: String, password: String) {
        if (!validar(email, password) || nombre.isBlank()) {
            _ui.value = _ui.value.copy(error = "Rellena nombre, email y una contraseña de al menos 6 caracteres.")
            return
        }
        lanzar { repo.registrar(nombre, email, password) }
    }

    fun iniciarSesion(email: String, password: String) {
        if (!validar(email, password)) {
            _ui.value = _ui.value.copy(error = "Introduce un email válido y la contraseña.")
            return
        }
        lanzar { repo.iniciarSesion(email, password) }
    }

    fun recuperarPassword(email: String) {
        if (email.isBlank()) {
            _ui.value = _ui.value.copy(error = "Escribe tu email para recuperar la contraseña.")
            return
        }
        viewModelScope.launch {
            _ui.value = AuthUiState(cargando = true)
            try {
                repo.enviarResetPassword(email)
                _ui.value = AuthUiState(mensaje = "Te hemos enviado un correo para restablecer la contraseña.")
            } catch (e: Exception) {
                _ui.value = AuthUiState(error = traducirError(e))
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

    /** Traduce los errores más habituales de Firebase a mensajes en español. */
    private fun traducirError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("password is invalid", true) ||
                msg.contains("credential is incorrect", true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", true) -> "Email o contraseña incorrectos."
            msg.contains("no user record", true) -> "No existe ninguna cuenta con ese email."
            msg.contains("email address is already in use", true) -> "Ya existe una cuenta con ese email."
            msg.contains("badly formatted", true) -> "El email no tiene un formato válido."
            msg.contains("network error", true) -> "Error de conexión. Comprueba tu internet."
            else -> "No se pudo completar la operación. Inténtalo de nuevo."
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repo) as T
    }
}
