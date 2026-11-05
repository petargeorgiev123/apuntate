package com.apuntate.cloud.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.apuntate.cloud.DetalleGrupoCloudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGrupoCloudScreen(
    viewModel: DetalleGrupoCloudViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var pestaña by remember { mutableIntStateOf(0) }
    var dialogoGasto by remember { mutableStateOf(false) }
    var dialogoSalir by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val grupo = state.grupo

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
                    // Compartir código de invitación
                    IconButton(onClick = {
                        val codigo = grupo?.codigoInvitacion ?: return@IconButton
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "¡Únete a nuestro grupo \"${grupo.nombre}\" en Apuntate! " +
                                    "Usa el código de invitación: $codigo"
                            )
                        }
                        context.startActivity(Intent.createChooser(envio, "Compartir código"))
                    }) {
                        Icon(Icons.Default.Share, "Compartir código", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { dialogoSalir = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Salir del grupo", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (pestaña == 1) {
                ExtendedFloatingActionButton(
                    onClick = { dialogoGasto = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nuevo gasto") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pestaña) {
                Tab(selected = pestaña == 0, onClick = { pestaña = 0 },
                    text = { Text("Grupo") }, icon = { Icon(Icons.Default.Groups, null) })
                Tab(selected = pestaña == 1, onClick = { pestaña = 1 },
                    text = { Text("Gastos") })
                Tab(selected = pestaña == 2, onClick = { pestaña = 2 },
                    text = { Text("Balance") })
                Tab(selected = pestaña == 3, onClick = { pestaña = 3 },
                    text = { Text("Chat") }, icon = { Icon(Icons.AutoMirrored.Filled.Chat, null) })
            }
            when (pestaña) {
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

    if (dialogoGasto) {
        DialogoGastoCloud(
            miembros = grupo?.miembros ?: emptyMap(),
            onConfirmar = { concepto, centimos, uid, nombre ->
                viewModel.añadirGasto(concepto, centimos, uid, nombre)
                dialogoGasto = false
            },
            onCancelar = { dialogoGasto = false }
        )
    }

    if (dialogoSalir) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { dialogoSalir = false },
            title = { Text("Salir del grupo") },
            text = { Text("¿Seguro que quieres salir de este grupo? Si eres el único miembro, el grupo se eliminará.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    dialogoSalir = false
                    viewModel.salirDelGrupo(onHecho = onVolver)
                }) { Text("Salir") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { dialogoSalir = false }) { Text("Cancelar") }
            }
        )
    }
}
