package com.apuntate.cloud.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.cloud.DetalleGrupoCloudUiState
import com.apuntate.ui.Moneda

// ---------------- Tab: Grupo (miembros + código) ----------------

@Composable
fun GrupoInfoTab(state: DetalleGrupoCloudUiState) {
    val grupo = state.grupo ?: return
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.codigo_invitacion), style = MaterialTheme.typography.labelLarge)
                    Text(
                        grupo.codigoInvitacion,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.codigo_ayuda),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.miembros_contador, grupo.miembros.size), style = MaterialTheme.typography.titleMedium)
        }
        items(grupo.miembros.entries.toList(), key = { it.key }) { (_, nombre) ->
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(nombre) },
                    leadingContent = { Icon(Icons.Default.Person, null) }
                )
            }
        }
    }
}

// ---------------- Tab: Gastos ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosCloudTab(
    state: DetalleGrupoCloudUiState,
    onEliminar: (String) -> Unit
) {
    val gastos = state.gastos
    if (gastos.isEmpty()) {
        MensajeVacioCloud(stringResource(R.string.sin_gastos))
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(gastos, key = { it.id }) { g ->
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(g.concepto, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(stringResource(R.string.pagado_por_etiqueta, g.pagadoPorNombre)) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Moneda.formatear(g.importeCentimos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onEliminar(g.id) }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.eliminar))
                            }
                        }
                    }
                )
            }
        }
    }
}

// ---------------- Tab: Balance ----------------

@Composable
fun BalanceCloudTab(state: DetalleGrupoCloudUiState) {
    val balance = state.balance
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.total_gastado), style = MaterialTheme.typography.labelLarge)
                    Text(
                        Moneda.formatear(balance.totalCentimos),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(stringResource(R.string.por_persona_etiqueta, Moneda.formatear(balance.porPersonaCentimos)))
                }
            }
        }
        if (balance.saldos.isNotEmpty()) {
            item { Text(stringResource(R.string.como_saldar), style = MaterialTheme.typography.titleMedium) }
            if (balance.pagos.isEmpty()) {
                item { Text(stringResource(R.string.todo_saldado)) }
            } else {
                items(balance.pagos) { pago ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${pago.de}  →  ${pago.a}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                Moneda.formatear(pago.cantidadCentimos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MensajeVacioCloud(mensaje: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(mensaje, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
