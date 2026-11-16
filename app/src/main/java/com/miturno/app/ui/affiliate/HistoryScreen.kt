package com.miturno.app.ui.affiliate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miturno.app.data.model.Turn
import com.miturno.app.ui.components.AppScaffold
import com.miturno.app.ui.components.StatusChip
import com.miturno.app.ui.components.TicketIcon
import com.miturno.app.ui.theme.Blue100
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.Ink
import com.miturno.app.ui.theme.OutlineBlue
import com.miturno.app.util.TimeUtils

@Composable
fun HistoryScreen(vm: HistoryViewModel, onBack: () -> Unit) {
    val turns by vm.turns.collectAsStateWithLifecycle()

    AppScaffold(title = "Mis turnos", onBack = onBack) { inner ->
        val list: List<Turn>? = turns
        when {
            list == null -> Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            list.isEmpty() -> Box(Modifier.fillMaxSize().padding(inner).padding(24.dp), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Blue100)
                ) {
                    Column(
                        Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = Blue700, modifier = Modifier.size(48.dp))
                        Text(
                            "Aún no has solicitado ningún turno.",
                            style = MaterialTheme.typography.titleLarge, color = Ink, textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(list, key = { it.id }) { TurnHistoryCard(it) }
            }
        }
    }
}

@Composable
private fun TurnHistoryCard(turn: Turn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Blue100)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
            TicketIcon(size = 40.dp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(turn.number, style = MaterialTheme.typography.headlineMedium, color = Blue700)
                    StatusChip(turn.status)
                }
                Text(turn.serviceName, style = MaterialTheme.typography.titleMedium, color = Blue900)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = Blue700, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(TimeUtils.dateTime(turn.requestedAt), style = MaterialTheme.typography.bodyMedium, color = Ink)
                }
            }
        }
    }
}
