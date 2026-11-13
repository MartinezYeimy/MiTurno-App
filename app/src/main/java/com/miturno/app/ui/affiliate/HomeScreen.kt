package com.miturno.app.ui.affiliate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.miturno.app.data.model.Progress
import com.miturno.app.data.model.Turn
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.ui.components.AppScaffold
import com.miturno.app.ui.components.BigButton
import com.miturno.app.ui.components.ButtonKind
import com.miturno.app.ui.components.ConfirmDialog
import com.miturno.app.ui.components.InfoCard
import com.miturno.app.ui.components.remainingText
import com.miturno.app.ui.theme.Blue100
import com.miturno.app.ui.theme.Blue50
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.Ink

//pantalla de funcionario solicitar turno o ver historial
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    flash: String?,
    onFlashShown: () -> Unit,
    onRequestTurn: () -> Unit,
    onHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val progress by vm.progress.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmCancel by remember { mutableStateOf(false) }

    LaunchedEffect(flash) {
        flash?.let {
            snackbar.showSnackbar(it)
            onFlashShown()
        }
    }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    AppScaffold(
        title = "MiTurno",
        snackbarHostState = snackbar,
        actions = {
            TextButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Ink)
                Spacer(Modifier.width(6.dp))
                Text("Salir", color = Ink)
            }
        }
    ) { inner ->
        val current: Progress? = progress
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Hola, ${vm.firstName}", style = MaterialTheme.typography.headlineLarge, color = Ink)

            when {
                current == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                current.turn == null -> {
                    NoTurnCard()
                    BigButton("Solicitar turno", onClick = onRequestTurn, icon = Icons.Filled.Add)
                }
                else -> ActiveTurnSection(current.turn, current, onCancel = { confirmCancel = true })
            }

            BigButton(
                "Ver historial de turnos", onClick = onHistory,
                icon = Icons.AutoMirrored.Filled.List, kind = ButtonKind.Secondary
            )
        }
    }

    val turn = progress?.turn
    if (confirmCancel && turn != null) {
        ConfirmDialog(
            title = "¿Cancelar tu turno?",
            text = "Perderás tu lugar en la fila del turno ${turn.number}. Esta acción no se puede deshacer.",
            confirmText = "Sí, cancelar",
            dismissText = "No, mantener",
            onConfirm = {
                confirmCancel = false
                vm.cancelTurn(turn.id)
            },
            onDismiss = { confirmCancel = false }
        )
    }
}

@Composable
private fun NoTurnCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blue50),
        border = BorderStroke(1.dp, Blue100)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No tienes un turno activo", style = MaterialTheme.typography.headlineSmall, color = Blue900)
            Text(
                "Pide tu turno desde aquí y espera donde prefieras. Te avisaremos cuando esté cerca.",
                style = MaterialTheme.typography.bodyLarge, color = Ink
            )
        }
    }
}

@Composable
private fun ActiveTurnSection(turn: Turn, progress: Progress, onCancel: () -> Unit) {
    // Tarjeta principal con el número de turno (RF-04)
    Surface(color = Blue700, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("TU TURNO", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(turn.number, style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text(
                turn.serviceName, style = MaterialTheme.typography.titleMedium,
                color = Color.White, textAlign = TextAlign.Center
            )
        }
    }

    if (turn.status == TurnStatus.IN_SERVICE) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blue100),
            border = BorderStroke(2.dp, Blue700)
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Blue900, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(14.dp))
                Text(
                    "¡Es tu turno! Dirígete al módulo de atención.",
                    style = MaterialTheme.typography.titleLarge, color = Blue900
                )
            }
        }
    } else {
        // RF-05: turno en atención / RF-06: turnos antes del tuyo
        InfoCard("Turno que se está atendiendo ahora", progress.current?.number ?: "Aún ninguno")
        InfoCard("Tu posición en la fila", remainingText(progress.ahead))
    }

    Text(
        "Esta información se actualiza sola. No necesitas cerrar ni volver a abrir la aplicación.",
        style = MaterialTheme.typography.bodyMedium, color = Ink
    )

    if (turn.status == TurnStatus.PENDING) {
        BigButton("Cancelar mi turno", onClick = onCancel, icon = Icons.Filled.Close, kind = ButtonKind.Danger)
    }
}
