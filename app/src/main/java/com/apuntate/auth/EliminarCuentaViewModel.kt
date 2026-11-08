package com.apuntate.auth

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apuntate.R
import com.apuntate.cloud.CloudRepository
import com.apuntate.perfil.FotoPerfilStore
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EliminarCuentaState(
    val eliminando: Boolean = false,
    @StringRes val error: Int? = null
)

/**
 * Borra la cuenta del usuario: confirma la contraseña, elimina sus datos en Firestore,
 * borra la foto local y, por último, elimina el usuario de Firebase Authentication.
 * Al terminar, el estado de sesión pasa a null y la app vuelve a la pantalla de login.
 */
class EliminarCuentaViewModel(
    private val auth: AuthRepository,
    private val cloud: CloudRepository,
    private val appContext: Context
) : ViewModel() {

    private val _estado = MutableStateFlow(EliminarCuentaState())
    val estado: StateFlow<EliminarCuentaState> = _estado.asStateFlow()

    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }

    fun eliminar(password: String) {
        if (password.isBlank()) {
            _estado.value = EliminarCuentaState(error = R.string.eliminar_cuenta_err_password)
            return
        }
        val uid = auth.usuarioActual()?.uid ?: return
        viewModelScope.launch {
            _estado.value = EliminarCuentaState(eliminando = true)
            try {
                // 1) Confirmar la contraseña antes de tocar ningún dato.
                auth.reautenticar(password)
                // 2) Borrar/anonimizar los datos del usuario en la nube.
                cloud.eliminarDatosDeUsuario(uid)
                // 3) Borrar la foto local.
                FotoPerfilStore.eliminar(appContext)
                // 4) Borrar la cuenta. El listener de sesión devolverá a la pantalla de login.
                auth.borrarUsuario()
                _estado.value = EliminarCuentaState()
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _estado.value = EliminarCuentaState(error = R.string.eliminar_cuenta_err_credenciales)
            } catch (e: FirebaseNetworkException) {
                _estado.value = EliminarCuentaState(error = R.string.auth_err_red)
            } catch (e: Exception) {
                _estado.value = EliminarCuentaState(error = R.string.auth_err_generico)
            }
        }
    }

    class Factory(
        private val auth: AuthRepository,
        private val cloud: CloudRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EliminarCuentaViewModel(auth, cloud, appContext) as T
    }
}
