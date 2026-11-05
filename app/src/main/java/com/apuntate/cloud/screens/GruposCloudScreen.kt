package com.apuntate.cloud.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apuntate.cloud.GrupoCloud
import com.apuntate.cloud.GruposCloudViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposCloudScreen(
    viewModel: GruposCloudViewModel,
    nombreUsuario: String,
    onCerrarSesion: () -> Unit,
    onAbrirGrupo: (String) -> Unit
) {
    val grupos by viewModel.grupos.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var menuUsuario by remember { mutableStateOf(false) }
    var dialogoCrear by remember { mutableStateOf(false) }
    var dialogoUnirse by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Mis grupos") },
                actions = {
                    IconButton(onClick = { menuUsuario = true }) {
                        Icon(Icons.Default.AccountCircle, "Cuenta", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    DropdownMenu(expanded = menuUsuario, onDismissRequest = { menuUsuario = false }) {
                        if (nombreUsuario.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(nombreUsuario, style = MaterialTheme.typography.labelLarge) },
                                onClick = {}, enabled = false
                            )
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                            onClick = { menuUsuario = false; onCerrarSesion() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = { dialogoUnirse = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) { Icon(Icons.Default.GroupAdd, "Unirse con código") }
                ExtendedFloatingActionButton(
                    onClick = { dialogoCrear = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nuevo grupo") }
                )
            }
        }
    ) { padding ->
        if (grupos.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Todavía no tienes grupos.\nCrea uno nuevo o únete con un código de invitación.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grupos, key = { it.id }) { grupo ->
                    GrupoCloudItem(grupo) { onAbrirGrupo(grupo.id) }
                }
            }
        }
    }

    if (dialogoCrear) {
        DialogoTextoSimple(
            titulo = "Nuevo grupo",
            etiqueta = "Nombre del grupo (ej. Esquí Sierra Nevada)",
            onConfirmar = { viewModel.crearGrupo(it); dialogoCrear = false },
            onCancelar = { dialogoCrear = false }
        )
    }
    if (dialogoUnirse) {
        DialogoTextoSimple(
            titulo = "Unirse a un grupo",
            etiqueta = "Código de invitación",
            forzarMayusculas = true,
            onConfirmar = { viewModel.unirseConCodigo(it); dialogoUnirse = false },
            onCancelar = { dialogoUnirse = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoCloudItem(grupo: GrupoCloud, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(grupo.nombre, style = MaterialTheme.typography.titleMedium) },
            supportingContent = {
                val n = grupo.miembrosUids.size
                Text("$n ${if (n == 1) "miembro" else "miembros"}")
            },
            leadingContent = { Icon(Icons.Default.Group, null) },
            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
