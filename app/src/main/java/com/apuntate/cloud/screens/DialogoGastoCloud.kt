package com.apuntate.cloud.screens

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apuntate.ui.Moneda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGastoCloud(
    miembros: Map<String, String>,
    onConfirmar: (concepto: String, importeCentimos: Long, uid: String, nombre: String) -> Unit,
    onCancelar: () -> Unit
) {
    val listaMiembros = remember(miembros) { miembros.entries.toList() }
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf(listaMiembros.firstOrNull()) }
    var menuAbierto by remember { mutableStateOf(false) }

    val centimos = Moneda.parsearACentimos(importe)
    val valido = concepto.isNotBlank() && centimos != null && centimos > 0 && seleccionado != null

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo gasto") },
        text = {
            Column {
                OutlinedTextField(
                    value = concepto,
                    onValueChange = { concepto = it },
                    label = { Text("Concepto (ej. Coche, Comida, Hotel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
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
                        value = seleccionado?.value ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pagado por") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        listaMiembros.forEach { entrada ->
                            DropdownMenuItem(
                                text = { Text(entrada.value) },
                                onClick = { seleccionado = entrada; menuAbierto = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val c = centimos; val m = seleccionado
                    if (valido && c != null && m != null) onConfirmar(concepto, c, m.key, m.value)
                },
                enabled = valido
            ) { Text("Registrar gasto") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
