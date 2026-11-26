package com.apuntate.auth

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

/** Usuario autenticado simplificado para la capa de UI. */
data class UsuarioActual(
    val uid: String,
    val email: String?,
    val nombre: String?
)

/**
 * Envuelve FirebaseAuth y expone las operaciones de login/registro como suspend functions,
 * además de un Flow con el estado de sesión actual.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /** Se incrementa cuando cambian datos del perfil (p. ej. el nombre) sin cambiar la sesión. */
    private val refrescos = MutableStateFlow(0)

    private val cambiosDeSesion: Flow<Unit> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(Unit) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Emite el usuario actual (o null) cuando cambia la sesión Y cuando cambia su perfil.
     * Firebase no avisa de los cambios de perfil: sin esto, tras registrarse la app se quedaba
     * con el nombre vacío (y mostraba la parte del email) hasta reiniciarla.
     */
    val usuario: Flow<UsuarioActual?> = combine(cambiosDeSesion, refrescos) { _, _ ->
        auth.currentUser?.toUsuarioActual()
    }.distinctUntilChanged()

    private fun notificarCambioPerfil() {
        refrescos.update { it + 1 }
    }

    fun usuarioActual(): UsuarioActual? = auth.currentUser?.toUsuarioActual()

    suspend fun registrar(nombre: String, email: String, password: String) {
        val resultado = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        // Guardamos el nombre visible en el perfil.
        resultado.user?.let { user ->
            val cambios = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(nombre.trim())
                .build()
            user.updateProfile(cambios).await()
            // Recargamos para que el nombre quede disponible de inmediato.
            user.reload().await()
            notificarCambioPerfil()
        }
    }

    suspend fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun enviarResetPassword(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    /** Actualiza el nombre visible del usuario en su perfil de Firebase. */
    suspend fun actualizarNombre(nombre: String) {
        val user = auth.currentUser ?: return
        val cambios = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(nombre.trim())
            .build()
        user.updateProfile(cambios).await()
        user.reload().await()
        notificarCambioPerfil()
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    /** Vuelve a verificar la contraseña (Firebase lo exige antes de borrar la cuenta). */
    suspend fun reautenticar(password: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Sin sesión")
        val email = user.email ?: throw IllegalStateException("Sin email")
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
    }

    /** Elimina definitivamente la cuenta de Firebase Authentication del usuario actual. */
    suspend fun borrarUsuario() {
        auth.currentUser?.delete()?.await()
    }

    private fun FirebaseUser.toUsuarioActual() = UsuarioActual(
        uid = uid,
        email = email,
        nombre = displayName
    )
}
