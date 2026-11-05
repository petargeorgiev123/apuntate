package com.apuntate.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /** Emite el usuario actual (o null) cada vez que cambia el estado de autenticación. */
    val usuario: Flow<UsuarioActual?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fb ->
            trySend(fb.currentUser?.toUsuarioActual())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
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
        }
    }

    suspend fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun enviarResetPassword(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    private fun FirebaseUser.toUsuarioActual() = UsuarioActual(
        uid = uid,
        email = email,
        nombre = displayName
    )
}
