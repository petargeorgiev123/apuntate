package com.apuntate.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.data.Participante
import com.apuntate.ui.Moneda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGasto(
    participantes: List<Participante>,
    onConfirmar: (concepto: String, importeCentimos: Long, pagadoPorId: Long) -> Unit,
    onCancelar: () -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var pagadoPor by remember { mutableStateOf(participantes.firstOrNull()) }
    var menuAbierto by remember { mutableStateOf(false) }

    val centimos = Moneda.parsearACentimos(importe)
    val valido = concepto.isNotBlank() && centimos != null && centimos > 0 && pagadoPor != null

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.nuevo_gasto)) },
        text = {
            Column {
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text(stringResource(R.string.concepto_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text(stringResource(R.string.importe_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = menuAbierto,
                    onExpandedChange = { menuAbierto = it },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = pagadoPor?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.pagado_por)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        participantes.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.nombre) },
                                onClick = {
                                    pagadoPor = p
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val c = centimos
                    val p = pagadoPor
                    if (valido && c != null && p != null) onConfirmar(concepto, c, p.id)
                },
                enabled = valido
            ) { Text(stringResource(R.string.registrar_gasto)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
