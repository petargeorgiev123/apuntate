package com.apuntate.cloud.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.ui.Moneda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGastoCloud(
    miembros: Map<String, String>,
    onConfirmar: (concepto: String, importeCentimos: Long, uid: String, nombre: String, participantes: List<String>) -> Unit,
    onCancelar: () -> Unit
) {
    val listaMiembros = remember(miembros) { miembros.entries.toList() }
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf(listaMiembros.firstOrNull()) }
    var menuAbierto by remember { mutableStateOf(false) }

    // Participantes del reparto: por defecto, todos marcados.
    val participantes = remember(miembros) {
        mutableStateMapOf<String, Boolean>().apply {
            listaMiembros.forEach { put(it.key, true) }
        }
    }
    val numSeleccionados = participantes.count { it.value }
    val estadoTodos = when (numSeleccionados) {
        0 -> ToggleableState.Off
        listaMiembros.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }

    val centimos = Moneda.parsearACentimos(importe)
    val valido = concepto.isNotBlank() && centimos != null && centimos > 0 &&
        seleccionado != null && numSeleccionados > 0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.nuevo_gasto)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                        value = seleccionado?.value ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.pagado_por)) },
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

                // Reparto: entre quiénes
                Text(
                    stringResource(R.string.reparto_entre),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                // Opción "Todos"
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = estadoTodos == ToggleableState.On,
                            onValueChange = { marcar ->
                                listaMiembros.forEach { participantes[it.key] = marcar }
                            }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TriStateCheckbox(state = estadoTodos, onClick = {
                        val marcar = estadoTodos != ToggleableState.On
                        listaMiembros.forEach { participantes[it.key] = marcar }
                    })
                    Text(stringResource(R.string.reparto_todos), fontWeight = FontWeight.Medium)
                }
                HorizontalDivider()
                // Lista de miembros con checkbox (altura limitada y desplazable si hay muchos)
                Column(Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                    listaMiembros.forEach { entrada ->
                        val marcado = participantes[entrada.key] == true
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = marcado,
                                    onValueChange = { participantes[entrada.key] = it }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = marcado, onCheckedChange = { participantes[entrada.key] = it })
                            Text(entrada.value)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val c = centimos; val m = seleccionado
                    val elegidos = participantes.filter { it.value }.keys.toList()
                    if (valido && c != null && m != null && elegidos.isNotEmpty()) {
                        onConfirmar(concepto, c, m.key, m.value, elegidos)
                    }
                },
                enabled = valido
            ) { Text(stringResource(R.string.registrar_gasto)) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) } }
    )
}
