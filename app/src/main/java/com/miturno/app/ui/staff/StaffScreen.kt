package com.miturno.app.ui.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miturno.app.data.model.Turn
import com.miturno.app.ui.components.AppScaffold
import com.miturno.app.ui.components.BigButton
import com.miturno.app.ui.components.ButtonKind
import com.miturno.app.ui.components.ConfirmDialog
import com.miturno.app.ui.components.GradientCard
import com.miturno.app.ui.components.TicketIcon
import com.miturno.app.ui.theme.Blue100
import com.miturno.app.ui.theme.Blue50
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.DangerRed
import com.miturno.app.ui.theme.Ink
import com.miturno.app.util.TimeUtils

@Composable
fun StaffScreen(vm: StaffViewModel, onLogout: () -> Unit) {
    val current by vm.current.collectAsStateWithLifecycle()
    val queue by vm.queue.collectAsStateWithLifecycle()
    val pointName by vm.pointName.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var turnToSkip by remember { mutableStateOf<Turn?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    AppScaffold(
        title = "Panel de atención",
        snackbarHostState = snackbar,
        actions = {
            TextButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Salir", color = Color.White)
            }
        }
    ) { inner ->
        val now: Turn? = current
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Blue100)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Blue700, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(pointName, style = MaterialTheme.typography.headlineSmall, color = Ink)
                            Text("Funcionario: ${vm.staffName}", style = MaterialTheme.typography.bodyMedium, color = Ink)
                            Text("Atiendes en: ${vm.module}", style = MaterialTheme.typography.titleMedium, color = Blue700)
                        }
                    }
                }
            }

            // Turno en atención
            item {
                GradientCard {
                    Text("ATENDIENDO AHORA", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    if (now == null) {
                        Text(
                            "Ningún turno en atención",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White, textAlign = TextAlign.Center
                        )
                    } else {
                        Text(now.number, style = MaterialTheme.typography.displayLarge, color = Color.White)
                        Text(
                            now.serviceName, style = MaterialTheme.typography.titleMedium,
                            color = Color.White, textAlign = TextAlign.Center
                        )
                        if (now.userName.isNotBlank()) {
                            Text(
                                "${now.userName} · Doc. ${now.userDocument}",
                                style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center
                            )
                        }
                        now.calledAt?.let {
                            Text("Llamado a las ${TimeUtils.time(it)}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }
                }
            }

            // Acciones
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BigButton(
                        "Llamar siguiente turno", onClick = { vm.callNext() },
                        icon = Icons.Filled.PlayArrow, enabled = queue.isNotEmpty()
                    )
                    BigButton(
                        "Marcar como atendido", onClick = { now?.let(vm::finish) },
                        icon = Icons.Filled.Check, kind = ButtonKind.Secondary, enabled = now != null
                    )
                    BigButton(
                        "Omitir (no se presentó)", onClick = { turnToSkip = now },
                        icon = Icons.Filled.Close, kind = ButtonKind.Danger, enabled = now != null
                    )
                }
            }

            // Fila de espera (RF-11)
            item {
                Text(
                    "Fila de espera (${queue.size})",
                    style = MaterialTheme.typography.headlineSmall, color = Blue900
                )
            }
            if (queue.isEmpty()) {
                item { Text("No hay turnos pendientes.", style = MaterialTheme.typography.bodyLarge, color = Ink) }
            }
            items(queue, key = { it.id }) { turn ->
                QueueRow(turn, onSkip = { turnToSkip = turn })
            }
        }
    }

    turnToSkip?.let { turn ->
        ConfirmDialog(
            title = "¿Omitir el turno ${turn.number}?",
            text = "El afiliado no podrá recuperar este turno y la fila continuará con el siguiente.",
            confirmText = "Sí, omitir",
            dismissText = "No",
            onConfirm = {
                turnToSkip = null
                vm.skip(turn)
            },
            onDismiss = { turnToSkip = null }
        )
    }
}

@Composable
private fun QueueRow(turn: Turn, onSkip: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Blue100)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TicketIcon(size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(turn.number, style = MaterialTheme.typography.headlineSmall, color = Blue700)
                if (turn.userName.isNotBlank()) {
                    Text("${turn.userName} · Doc. ${turn.userDocument}", style = MaterialTheme.typography.titleSmall, color = Ink)
                }
                Text(turn.serviceName, style = MaterialTheme.typography.bodyMedium, color = Ink)
                Text("Solicitado a las ${TimeUtils.time(turn.requestedAt)}", style = MaterialTheme.typography.bodySmall, color = Ink)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onSkip,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, DangerRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
            ) { Text("Omitir") }
        }
    }
}
