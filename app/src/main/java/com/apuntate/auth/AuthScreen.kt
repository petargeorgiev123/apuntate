package com.apuntate.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apuntate.R

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val ui by viewModel.ui.collectAsState()

    var esRegistro by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (esRegistro) stringResource(R.string.auth_subtitulo_registro)
            else stringResource(R.string.auth_subtitulo_login),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = esRegistro) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.auth_nombre)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (esRegistro) Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            singleLine = true,
            visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        imageVector = if (verPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (verPassword) stringResource(R.string.auth_ocultar_password)
                        else stringResource(R.string.auth_mostrar_password)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (!esRegistro) {
            TextButton(
                onClick = { viewModel.recuperarPassword(email) },
                modifier = Modifier.align(Alignment.End)
            ) { Text(stringResource(R.string.auth_olvido_password)) }
        }

        Spacer(Modifier.height(8.dp))

        ui.error?.let {
            Text(stringResource(it.textoRes()), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }
        ui.info?.let {
            Text(stringResource(it.textoRes()), color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.limpiarMensajes()
                if (esRegistro) viewModel.registrar(nombre, email, password)
                else viewModel.iniciarSesion(email, password)
            },
            enabled = !ui.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (ui.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (esRegistro) stringResource(R.string.auth_registrarse) else stringResource(R.string.auth_entrar))
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            esRegistro = !esRegistro
            viewModel.limpiarMensajes()
        }) {
            Text(
                if (esRegistro) stringResource(R.string.auth_cambiar_a_login)
                else stringResource(R.string.auth_cambiar_a_registro)
            )
        }
    }
}
