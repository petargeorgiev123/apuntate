package com.apuntate.cloud.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.i18n.Idioma
import com.apuntate.i18n.LocaleManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    nombreActual: String,
    email: String,
    onGuardarNombre: (String, onHecho: () -> Unit) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    val snackbar = remember { SnackbarHostState() }
    val mensajeGuardado = stringResource(R.string.perfil_nombre_guardado)
    var idiomaSeleccionado by remember { mutableStateOf(LocaleManager.idiomaActual()) }
    var pedirSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(pedirSnackbar) {
        if (pedirSnackbar) {
            snackbar.showSnackbar(mensajeGuardado)
            pedirSnackbar = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perfil_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.perfil_email_etiqueta, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Editar nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.perfil_nombre)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onGuardarNombre(nombre) { pedirSnackbar = true } },
                enabled = nombre.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.perfil_guardar_nombre)) }

            // Selector de idioma
            Text(
                stringResource(R.string.perfil_idioma),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(Modifier.fillMaxWidth()) {
                Column {
                    OpcionIdioma(
                        texto = stringResource(R.string.perfil_idioma_es),
                        seleccionado = idiomaSeleccionado == Idioma.ESPANOL,
                        onClick = { idiomaSeleccionado = Idioma.ESPANOL; LocaleManager.aplicar(Idioma.ESPANOL) }
                    )
                    OpcionIdioma(
                        texto = stringResource(R.string.perfil_idioma_en),
                        seleccionado = idiomaSeleccionado == Idioma.INGLES,
                        onClick = { idiomaSeleccionado = Idioma.INGLES; LocaleManager.aplicar(Idioma.INGLES) }
                    )
                    OpcionIdioma(
                        texto = stringResource(R.string.perfil_idioma_bg),
                        seleccionado = idiomaSeleccionado == Idioma.BULGARO,
                        onClick = { idiomaSeleccionado = Idioma.BULGARO; LocaleManager.aplicar(Idioma.BULGARO) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OpcionIdioma(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionado, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionado, onClick = onClick)
        Text(texto, Modifier.padding(start = 8.dp))
    }
}
