package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apuntate.cloud.CloudRepository
import com.apuntate.cloud.DetalleGrupoCloudViewModel
import com.apuntate.cloud.GruposCloudViewModel
import com.apuntate.cloud.screens.DetalleGrupoCloudScreen
import com.apuntate.cloud.screens.GruposCloudScreen

object Rutas {
    const val GRUPOS = "grupos"
    const val DETALLE = "detalle/{grupoId}"
    fun detalle(grupoId: String) = "detalle/$grupoId"
}

@Composable
fun ApuntateApp(
    cloudRepo: CloudRepository,
    uid: String,
    nombreUsuario: String,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.GRUPOS) {
        composable(Rutas.GRUPOS) {
            val vm: GruposCloudViewModel = viewModel(
                factory = GruposCloudViewModel.Factory(cloudRepo, uid, nombreUsuario)
            )
            GruposCloudScreen(
                viewModel = vm,
                nombreUsuario = nombreUsuario,
                onCerrarSesion = onCerrarSesion,
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
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
