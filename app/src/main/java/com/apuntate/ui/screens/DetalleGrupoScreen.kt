package com.apuntate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apuntate.R
import com.apuntate.data.Gasto
import com.apuntate.data.Participante
import com.apuntate.domain.Pago
import com.apuntate.ui.DetalleGrupoViewModel
import com.apuntate.ui.Moneda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleGrupoScreen(
    viewModel: DetalleGrupoViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var pestaña by remember { mutableIntStateOf(0) }
    var mostrarDialogoParticipante by remember { mutableStateOf(false) }
    var mostrarDialogoGasto by remember { mutableStateOf(false) }

    val titulos = listOf(
        stringResource(R.string.participantes),
        stringResource(R.string.gastos),
        stringResource(R.string.balance)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.grupo?.nombre ?: "") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            when (pestaña) {
                0 -> ExtendedFloatingActionButton(
                    onClick = { mostrarDialogoParticipante = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.añadir_participante)) }
                )
                1 -> ExtendedFloatingActionButton(
                    onClick = { mostrarDialogoGasto = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.nuevo_gasto)) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pestaña) {
                titulos.forEachIndexed { index, titulo ->
                    Tab(
                        selected = pestaña == index,
                        onClick = { pestaña = index },
                        text = { Text(titulo) }
                    )
                }
            }
            when (pestaña) {
                0 -> ParticipantesTab(
                    participantes = state.participantes,
                    onEliminar = viewModel::eliminarParticipante
                )
                1 -> GastosTab(
                    gastos = state.gastos,
                    participantes = state.participantes,
                    onEliminar = viewModel::eliminarGasto
                )
                2 -> BalanceTab(state)
            }
        }
    }

    if (mostrarDialogoParticipante) {
        DialogoTexto(
            titulo = stringResource(R.string.añadir_participante),
            etiqueta = stringResource(R.string.nombre_participante_hint),
            onConfirmar = {
                viewModel.añadirParticipante(it)
                mostrarDialogoParticipante = false
            },
            onCancelar = { mostrarDialogoParticipante = false }
        )
    }

    if (mostrarDialogoGasto) {
        DialogoGasto(
            participantes = state.participantes,
            onConfirmar = { concepto, centimos, pagadoPorId ->
                viewModel.añadirGasto(concepto, centimos, pagadoPorId)
                mostrarDialogoGasto = false
            },
            onCancelar = { mostrarDialogoGasto = false }
        )
    }
}

// ---------------- Pestaña: Participantes ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantesTab(
    participantes: List<Participante>,
    onEliminar: (Participante) -> Unit
) {
    if (participantes.isEmpty()) {
        MensajeVacio(stringResource(R.string.sin_participantes))
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(participantes, key = { it.id }) { p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(p.nombre) },
                    trailingContent = {
                        IconButton(onClick = { onEliminar(p) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar))
                        }
                    }
                )
            }
        }
    }
}

// ---------------- Pestaña: Gastos ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GastosTab(
    gastos: List<Gasto>,
    participantes: List<Participante>,
    onEliminar: (Gasto) -> Unit
) {
    if (gastos.isEmpty()) {
        MensajeVacio(stringResource(R.string.sin_gastos))
        return
    }
    val nombrePorId = participantes.associate { it.id to it.nombre }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(gastos, key = { it.id }) { g ->
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(g.concepto, style = MaterialTheme.typography.titleMedium) },
                    supportingContent = {
                        Text("${stringResource(R.string.pagado_por)}: ${nombrePorId[g.pagadoPorId] ?: "?"}")
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                Moneda.formatear(g.importeCentimos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onEliminar(g) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar))
                            }
                        }
                    }
                )
            }
        }
    }
}

// ---------------- Pestaña: Balance ----------------

@Composable
private fun BalanceTab(state: com.apuntate.ui.DetalleGrupoUiState) {
    val balance = state.balance
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.total_gastado), style = MaterialTheme.typography.labelLarge)
                    Text(
                        Moneda.formatear(balance.totalCentimos),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.participantes.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            "${stringResource(R.string.por_persona)}: ${Moneda.formatear(balance.porPersonaCentimos)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (balance.saldos.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.como_saldar),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (balance.pagos.isEmpty()) {
                item { Text(stringResource(R.string.todo_saldado)) }
            } else {
                items(balance.pagos) { pago -> PagoItem(pago) }
            }
        }
    }
}

@Composable
private fun PagoItem(pago: Pago) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${pago.de}  →  ${pago.a}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                Moneda.formatear(pago.cantidadCentimos),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------- Común ----------------

@Composable
private fun MensajeVacio(mensaje: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}
