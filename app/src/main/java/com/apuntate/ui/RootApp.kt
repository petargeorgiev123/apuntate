package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apuntate.auth.AuthRepository
import com.apuntate.auth.AuthScreen
import com.apuntate.auth.AuthViewModel
import com.apuntate.cloud.CloudRepository
import com.apuntate.notif.MensajesObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

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
    val context = LocalContext.current

    val u = usuario
    if (u == null) {
        AuthScreen(viewModel = authViewModel)
    } else {
        // Observador de mensajes para notificaciones locales, activo mientras hay sesión.
        val observer = remember(u.uid) {
            MensajesObserver(
                context = context.applicationContext,
                repo = cloudRepo,
                uid = u.uid,
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            )
        }
        DisposableEffect(u.uid) {
            observer.iniciar()
            onDispose {
                observer.detener()
                observer.scope.cancel()
            }
        }

        ApuntateApp(
            authRepo = authRepo,
            cloudRepo = cloudRepo,
            uid = u.uid,
            nombreUsuario = u.nombre ?: u.email ?: "Usuario",
            email = u.email ?: "",
            observer = observer,
            onCerrarSesion = { authViewModel.cerrarSesion() }
        )
    }
}
