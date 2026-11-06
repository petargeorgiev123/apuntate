package com.apuntate.cloud.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.apuntate.R
import com.apuntate.cloud.DetalleGrupoCloudViewModel
import com.apuntate.notif.MensajesObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGrupoCloudScreen(
    viewModel: DetalleGrupoCloudViewModel,
    observer: MensajesObserver,
    grupoId: String,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val pestaña = pagerState.currentPage
    val scope = rememberCoroutineScope()
    var dialogoGasto by remember { mutableStateOf(false) }
    var dialogoSalir by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val grupo = state.grupo

    // Mientras estamos en la pestaña de chat de este grupo, no notificar sus mensajes.
    DisposableEffect(pestaña, grupoId) {
        observer.grupoAbiertoId = if (pestaña == 3) grupoId else null
        onDispose { observer.grupoAbiertoId = null }
    }

    val plantillaCompartir = stringResource(R.string.compartir_mensaje, grupo?.nombre ?: "", grupo?.codigoInvitacion ?: "")
    val tituloCompartir = stringResource(R.string.compartir_codigo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(grupo?.nombre ?: "") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        grupo ?: return@IconButton
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, plantillaCompartir)
                        }
                        context.startActivity(Intent.createChooser(envio, tituloCompartir))
                    }) {
                        Icon(Icons.Default.Share, stringResource(R.string.compartir_codigo))
                    }
                    IconButton(onClick = { dialogoSalir = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, stringResource(R.string.salir_grupo))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (pestaña == 1) {
                ExtendedFloatingActionButton(
                    onClick = { dialogoGasto = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.nuevo_gasto)) }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pestaña) {
                val irA: (Int) -> Unit = { destino -> scope.launch { pagerState.animateScrollToPage(destino) } }
                Tab(selected = pestaña == 0, onClick = { irA(0) },
                    text = { Text(stringResource(R.string.tab_grupo)) }, icon = { Icon(Icons.Default.Groups, null) })
                Tab(selected = pestaña == 1, onClick = { irA(1) },
                    text = { Text(stringResource(R.string.tab_gastos)) })
                Tab(selected = pestaña == 2, onClick = { irA(2) },
                    text = { Text(stringResource(R.string.tab_balance)) })
                Tab(selected = pestaña == 3, onClick = { irA(3) },
                    text = { Text(stringResource(R.string.tab_chat)) }, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) })
            }
            // Deslizar horizontalmente para cambiar de pestaña.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pagina ->
                when (pagina) {
                    0 -> GrupoInfoTab(state)
                    1 -> GastosCloudTab(state, onEliminar = viewModel::eliminarGasto)
                    2 -> BalanceCloudTab(state)
                    3 -> ChatTab(
                        mensajes = state.mensajes,
                        miUid = viewModel.uid,
                        onEnviar = viewModel::enviarMensaje
                    )
                }
            }
        }
    }

    if (dialogoGasto) {
        DialogoGastoCloud(
            miembros = grupo?.miembros ?: emptyMap(),
            onConfirmar = { concepto, centimos, uid, nombre, participantes ->
                viewModel.añadirGasto(concepto, centimos, uid, nombre, participantes)
                dialogoGasto = false
            },
            onCancelar = { dialogoGasto = false }
        )
    }

    if (dialogoSalir) {
        AlertDialog(
            onDismissRequest = { dialogoSalir = false },
            title = { Text(stringResource(R.string.salir_grupo)) },
            text = { Text(stringResource(R.string.salir_grupo_confirmar)) },
            confirmButton = {
                TextButton(onClick = {
                    dialogoSalir = false
                    viewModel.salirDelGrupo(onHecho = onVolver)
                }) { Text(stringResource(R.string.salir)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoSalir = false }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}
