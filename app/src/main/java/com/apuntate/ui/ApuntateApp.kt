package com.apuntate.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apuntate.data.ApuntateRepository
import com.apuntate.ui.screens.DetalleGrupoScreen
import com.apuntate.ui.screens.GruposScreen

object Rutas {
    const val GRUPOS = "grupos"
    const val DETALLE = "detalle/{grupoId}"
    fun detalle(grupoId: Long) = "detalle/$grupoId"
}

@Composable
fun ApuntateApp(
    repo: ApuntateRepository,
    nombreUsuario: String,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rutas.GRUPOS) {
        composable(Rutas.GRUPOS) {
            val vm: GruposViewModel = viewModel(factory = GruposViewModel.Factory(repo))
            GruposScreen(
                viewModel = vm,
                nombreUsuario = nombreUsuario,
                onCerrarSesion = onCerrarSesion,
                onAbrirGrupo = { grupoId -> navController.navigate(Rutas.detalle(grupoId)) }
            )
        }
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("grupoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getLong("grupoId") ?: return@composable
            val vm: DetalleGrupoViewModel = viewModel(
                factory = DetalleGrupoViewModel.Factory(repo, grupoId)
            )
            DetalleGrupoScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
