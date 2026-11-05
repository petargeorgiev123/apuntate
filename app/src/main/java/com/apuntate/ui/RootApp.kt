package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntate.auth.AuthRepository
import com.apuntate.auth.AuthScreen
import com.apuntate.auth.AuthViewModel
import com.apuntate.data.ApuntateRepository

/**
 * Punto de entrada de la UI. Decide, según el estado de sesión de Firebase,
 * si mostrar la pantalla de login o la aplicación principal.
 */
@Composable
fun RootApp(
    repo: ApuntateRepository,
    authRepo: AuthRepository
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepo))
    val usuario by authViewModel.usuario.collectAsState()

    if (usuario == null) {
        AuthScreen(viewModel = authViewModel)
    } else {
        ApuntateApp(
            repo = repo,
            nombreUsuario = usuario?.nombre ?: usuario?.email ?: "",
            onCerrarSesion = { authViewModel.cerrarSesion() }
        )
    }
}
