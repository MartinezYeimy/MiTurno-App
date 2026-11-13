package com.miturno.app.ui.affiliate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miturno.app.data.model.Point
import com.miturno.app.data.model.ServiceItem
import com.miturno.app.ui.components.AppScaffold
import com.miturno.app.ui.components.ConfirmDialog
import com.miturno.app.ui.components.NoticeCard
import com.miturno.app.ui.theme.Blue100
import com.miturno.app.ui.theme.Blue50
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.Ink
import com.miturno.app.ui.theme.OutlineBlue

//solicitar turnos elección de sedes y servicios
@Composable
fun ServicesScreen(
    vm: ServicesViewModel,
    onBack: () -> Unit,
    onRequested: (String) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pending by remember { mutableStateOf<ServiceItem?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    AppScaffold(title = "Solicitar turno", onBack = onBack, snackbarHostState = snackbar) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("¿Qué trámite necesitas?", style = MaterialTheme.typography.headlineMedium, color = Ink)
            }
            if (ui.hasActiveTurn) {
                item {
                    NoticeCard("Ya tienes un turno activo. Para pedir otro, primero cancélalo o espera a que te atiendan.")
                }
            }
            item { Text("1. Elige el punto de atención", style = MaterialTheme.typography.titleMedium, color = Blue900) }
            items(ui.points, key = { "point-${it.id}" }) { point ->
                PointOption(point, selected = point.id == ui.selectedPointId) { vm.selectPoint(point.id) }
            }
            item { Text("2. Elige el servicio", style = MaterialTheme.typography.titleMedium, color = Blue900) }
            if (ui.services.isEmpty()) {
                item { Text("Cargando servicios…", style = MaterialTheme.typography.bodyLarge, color = Ink) }
            }
            items(ui.services, key = { "service-${it.id}" }) { service ->
                ServiceCard(service, enabled = !ui.hasActiveTurn && !busy) { pending = service }
            }
        }
    }

    pending?.let { service ->
        val pointName = ui.points.firstOrNull { it.id == service.pointId }?.name ?: "el punto seleccionado"
        ConfirmDialog(
            title = "¿Pedir este turno?",
            text = "Servicio: ${service.name}\nPunto de atención: $pointName",
            confirmText = "Sí, pedir turno",
            dismissText = "Volver",
            onConfirm = {
                pending = null
                vm.requestTurn(service) { number ->
                    onRequested("Turno solicitado correctamente. Tu número es $number")
                }
            },
            onDismiss = { pending = null }
        )
    }
}

@Composable
private fun PointOption(point: Point, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) Blue100 else Color.White),
        border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) Blue700 else OutlineBlue)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(point.name, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(point.address, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.Check, contentDescription = "Seleccionado", tint = Blue700, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun ServiceCard(service: ServiceItem, enabled: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Blue50,
            disabledContainerColor = Color(0xFFF3F3F3)
        ),
        border = BorderStroke(1.dp, if (enabled) Blue700 else OutlineBlue)
    ) {
        Column(Modifier.padding(18.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(service.name, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(service.description, style = MaterialTheme.typography.bodyMedium, color = Ink)
            Text(
                if (enabled) "Toca para pedir turno" else "No disponible ahora",
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) Blue700 else Ink
            )
        }
    }
}
