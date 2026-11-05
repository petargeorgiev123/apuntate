package com.apuntate.cloud.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.apuntate.R

/** Diálogo genérico con un único campo de texto, reutilizado por las pantallas cloud. */
@Composable
fun DialogoTextoSimple(
    titulo: String,
    etiqueta: String,
    forzarMayusculas: Boolean = false,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = if (forzarMayusculas) it.uppercase() else it },
                label = { Text(etiqueta) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = if (forzarMayusculas) KeyboardCapitalization.Characters
                    else KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (texto.isNotBlank()) onConfirmar(texto) }, enabled = texto.isNotBlank()) {
                Text(stringResource(R.string.aceptar))
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) } }
    )
}
