package com.apuntate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.data.Grupo
import com.apuntate.ui.GruposViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposScreen(
    viewModel: GruposViewModel,
    nombreUsuario: String,
    onCerrarSesion: () -> Unit,
    onAbrirGrupo: (Long) -> Unit
) {
    val grupos by viewModel.grupos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var menuUsuario by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.grupos_titulo)) },
                actions = {
                    IconButton(onClick = { menuUsuario = true }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Cuenta",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = menuUsuario,
                        onDismissRequest = { menuUsuario = false }
                    ) {
                        if (nombreUsuario.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(nombreUsuario, style = MaterialTheme.typography.labelLarge) },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider()
                        }
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            onClick = {
                                menuUsuario = false
                                onCerrarSesion()
                            }
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
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogo = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.nuevo_grupo)) }
            )
        }
    ) { padding ->
        if (grupos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.sin_grupos),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grupos, key = { it.id }) { grupo ->
                    GrupoItem(
                        grupo = grupo,
                        onClick = { onAbrirGrupo(grupo.id) },
                        onEliminar = { viewModel.eliminarGrupo(grupo) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        DialogoTexto(
            titulo = stringResource(R.string.crear_grupo),
            etiqueta = stringResource(R.string.nombre_grupo_hint),
            onConfirmar = {
                viewModel.crearGrupo(it)
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoItem(
    grupo: Grupo,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(grupo.nombre, style = MaterialTheme.typography.titleMedium) },
            leadingContent = { Icon(Icons.Default.Groups, contentDescription = null) },
            trailingContent = {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            },
            modifier = Modifier.clickableRipple(onClick)
        )
    }
}
