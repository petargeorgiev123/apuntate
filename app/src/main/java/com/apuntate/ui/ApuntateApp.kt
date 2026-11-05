package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apuntate.auth.AuthRepository
import com.apuntate.auth.AuthViewModel
import com.apuntate.cloud.CloudRepository
import com.apuntate.cloud.DetalleGrupoCloudViewModel
import com.apuntate.cloud.GruposCloudViewModel
import com.apuntate.cloud.screens.DetalleGrupoCloudScreen
import com.apuntate.cloud.screens.GruposCloudScreen
import com.apuntate.cloud.screens.PerfilScreen
import com.apuntate.notif.MensajesObserver

object Rutas {
    const val GRUPOS = "grupos"
    const val DETALLE = "detalle/{grupoId}"
    const val PERFIL = "perfil"
    fun detalle(grupoId: String) = "detalle/$grupoId"
}

@Composable
fun ApuntateApp(
    authRepo: AuthRepository,
    cloudRepo: CloudRepository,
    uid: String,
    nombreUsuario: String,
    email: String,
    observer: MensajesObserver,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authRepo))

    NavHost(navController = navController, startDestination = Rutas.GRUPOS) {
        composable(Rutas.GRUPOS) {
            val vm: GruposCloudViewModel = viewModel(
                factory = GruposCloudViewModel.Factory(cloudRepo, uid, nombreUsuario)
            )
            GruposCloudScreen(
                viewModel = vm,
                nombreUsuario = nombreUsuario,
                onCerrarSesion = onCerrarSesion,
                onAbrirPerfil = { navController.navigate(Rutas.PERFIL) },
                onAbrirGrupo = { grupoId -> navController.navigate(Rutas.detalle(grupoId)) }
            )
        }
        composable(Rutas.DETALLE) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: return@composable
            val vm: DetalleGrupoCloudViewModel = viewModel(
                factory = DetalleGrupoCloudViewModel.Factory(cloudRepo, grupoId, uid, nombreUsuario)
            )
            DetalleGrupoCloudScreen(
                viewModel = vm,
                observer = observer,
                grupoId = grupoId,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.PERFIL) {
            PerfilScreen(
                nombreActual = nombreUsuario,
                email = email,
                onGuardarNombre = { nuevoNombre, onHecho ->
                    authViewModel.actualizarNombre(nuevoNombre, onHecho)
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
