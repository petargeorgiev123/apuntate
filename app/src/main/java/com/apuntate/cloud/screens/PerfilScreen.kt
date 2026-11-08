package com.apuntate.cloud.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.i18n.Idioma
import com.apuntate.i18n.LocaleManager
import com.apuntate.perfil.FotoPerfilStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    nombreActual: String,
    email: String,
    onGuardarNombre: (String, onHecho: () -> Unit) -> Unit,
    eliminando: Boolean,
    errorEliminar: Int?,
    onEliminarCuenta: (String) -> Unit,
    onLimpiarErrorEliminar: () -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    var dialogoEliminar by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val mensajeGuardado = stringResource(R.string.perfil_nombre_guardado)
    var idiomaSeleccionado by remember { mutableStateOf(LocaleManager.idiomaActual()) }
    var pedirSnackbar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Versión que se incrementa al cambiar la foto, para forzar recarga del avatar.
    var versionFoto by remember { mutableStateOf(0) }

    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && FotoPerfilStore.guardarDesdeUri(context, uri)) {
            versionFoto++
        }
    }

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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar + selector de foto
            AvatarPerfil(
                versionFoto = versionFoto,
                onCambiar = {
                    selectorFoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onQuitar = {
                    FotoPerfilStore.eliminar(context)
                    versionFoto++
                }
            )

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

            // Eliminar cuenta
            OutlinedButton(
                onClick = { onLimpiarErrorEliminar(); dialogoEliminar = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) { Text(stringResource(R.string.eliminar_cuenta)) }
        }
    }

    if (dialogoEliminar) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { if (!eliminando) dialogoEliminar = false },
            title = { Text(stringResource(R.string.eliminar_cuenta_titulo)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.eliminar_cuenta_aviso))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; onLimpiarErrorEliminar() },
                        label = { Text(stringResource(R.string.eliminar_cuenta_password)) },
                        singleLine = true,
                        enabled = !eliminando,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = errorEliminar != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorEliminar?.let {
                        Text(
                            stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEliminarCuenta(password) },
                    enabled = !eliminando,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.eliminar_cuenta_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogoEliminar = false }, enabled = !eliminando) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

@Composable
private fun AvatarPerfil(
    versionFoto: Int,
    onCambiar: () -> Unit,
    onQuitar: () -> Unit
) {
    val context = LocalContext.current
    // Recarga el bitmap cuando cambia la versión.
    val bitmap = remember(versionFoto) {
        FotoPerfilStore.obtener(context)?.let { f ->
            try {
                BitmapFactory.decodeFile(f.absolutePath)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onCambiar),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.perfil_foto),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = stringResource(R.string.perfil_foto),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCambiar) {
                Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp))
                Text(" " + stringResource(R.string.perfil_cambiar_foto))
            }
            if (bitmap != null) {
                TextButton(onClick = onQuitar) {
                    Text(stringResource(R.string.perfil_quitar_foto))
                }
            }
        }
        Text(
            stringResource(R.string.perfil_foto_local),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
