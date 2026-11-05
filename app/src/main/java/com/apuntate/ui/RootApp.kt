package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntate.auth.AuthRepository
import com.apuntate.auth.AuthScreen
import com.apuntate.auth.AuthViewModel
import com.apuntate.cloud.CloudRepository

/**
 * Punto de entrada de la UI. Según el estado de sesión de Firebase, muestra el login
 * o la aplicación principal (versión en la nube con grupos compartidos y chat).
 */
@Composable
fun RootApp(
    authRepo: AuthRepository,
    cloudRepo: CloudRepository
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepo))
    val usuario by authViewModel.usuario.collectAsState()

    val u = usuario
    if (u == null) {
        AuthScreen(viewModel = authViewModel)
    } else {
        ApuntateApp(
            cloudRepo = cloudRepo,
            uid = u.uid,
            nombreUsuario = u.nombre ?: u.email ?: "Usuario",
            onCerrarSesion = { authViewModel.cerrarSesion() }
        )
    }
}
