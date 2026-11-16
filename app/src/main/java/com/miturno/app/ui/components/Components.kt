package com.miturno.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miturno.app.data.model.TurnStatus
import com.miturno.app.ui.theme.Blue100
import com.miturno.app.ui.theme.BluePrimary
import com.miturno.app.ui.theme.DangerBg
import com.miturno.app.ui.theme.Blue50
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.DangerRed
import com.miturno.app.ui.theme.Ink
import com.miturno.app.ui.theme.InkSoft
import com.miturno.app.ui.theme.NeutralBg
import com.miturno.app.ui.theme.OutlineBlue
import com.miturno.app.ui.theme.SuccessBg
import com.miturno.app.ui.theme.SuccessGreen

enum class ButtonKind { Primary, Secondary, Danger }

/** Botón grande y redondeado (mínimo 60 dp de alto) con texto claro; fácil de tocar para cualquier persona. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    kind: ButtonKind = ButtonKind.Primary,
    enabled: Boolean = true
) {
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    val m = modifier.fillMaxWidth().heightIn(min = 60.dp)
    val shape = RoundedCornerShape(20.dp)
    when (kind) {
        ButtonKind.Primary -> Button(
            onClick = onClick, modifier = m, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue700,
                contentColor = Color.White,
                disabledContainerColor = Blue100,
                disabledContentColor = Color(0xFF6B84A3)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp, pressedElevation = 1.dp, disabledElevation = 0.dp
            ),
            content = content
        )
        ButtonKind.Secondary -> Button(
            onClick = onClick, modifier = m, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue100,
                contentColor = Blue900,
                disabledContainerColor = Blue50,
                disabledContentColor = Color(0xFF6B84A3)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp, pressedElevation = 0.dp, disabledElevation = 0.dp
            ),
            border = BorderStroke(1.dp, if (enabled) OutlineBlue else Blue100),
            content = content
        )
        ButtonKind.Danger -> OutlinedButton(
            onClick = onClick, modifier = m, enabled = enabled, shape = shape,
            border = BorderStroke(1.5.dp, if (enabled) DangerRed else OutlineBlue),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (enabled) DangerBg.copy(alpha = 0.45f) else Color.Transparent,
                contentColor = DangerRed
            ),
            content = content
        )
    }
}

/** Estructura estándar de pantalla: encabezado azul ondulado + contenido sobre fondo claro + avisos (snackbar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit
) {
    AppBackground(modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, WaveHeaderShape, clip = false)
                        .background(BluePrimary, WaveHeaderShape)
                ) {
                    TopAppBar(
                        title = { Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White) },
                        navigationIcon = {
                            if (onBack != null) {
                                IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        },
                        actions = actions,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(18.dp))
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            content = content
        )
    }
}

/** Campo de texto grande. Si es contraseña muestra el botón "Mostrar / Ocultar" con texto (más claro que un ícono). */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null
) {
    var visible by remember { mutableStateOf(false) }
    val trailing: (@Composable () -> Unit)? = if (isPassword) {
        {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Ocultar" else "Mostrar")
            }
        }
    } else null
    val leading: (@Composable () -> Unit)? = if (leadingIcon != null) {
        { Icon(leadingIcon, contentDescription = null, tint = Blue700) }
    } else null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        leadingIcon = leading,
        trailingIcon = trailing,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = OutlineBlue,
            focusedLabelColor = Blue700,
            unfocusedLabelColor = InkSoft,
            cursorColor = Blue700
        )
    )
}

/** Tarjeta de información con título pequeño y valor grande. */
@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Blue100)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = Blue900)
        }
    }
}

/** Mensaje de error / aviso con ícono y texto (no depende solo del color). */
@Composable
fun NoticeCard(text: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    val border = if (isError) DangerRed else Blue700
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, border)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = border, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

fun statusLabel(status: String): String = when (status) {
    TurnStatus.PENDING -> "En espera"
    TurnStatus.IN_SERVICE -> "En atención"
    TurnStatus.DONE -> "Atendido"
    TurnStatus.CANCELLED -> "Cancelado"
    TurnStatus.SKIPPED -> "Omitido"
    else -> status
}

/** Etiqueta del estado: siempre lleva texto, el color solo refuerza. */
@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        TurnStatus.IN_SERVICE -> Blue700 to Color.White
        TurnStatus.PENDING -> Blue100 to Blue900
        TurnStatus.DONE -> SuccessBg to SuccessGreen
        TurnStatus.SKIPPED, TurnStatus.CANCELLED -> DangerBg to DangerRed
        else -> NeutralBg to Ink
    }
    Text(
        text = statusLabel(status),
        style = MaterialTheme.typography.titleSmall,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, color = Ink) },
        text = { Text(text, style = MaterialTheme.typography.bodyLarge, color = Ink) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } }
    )
}

fun remainingText(ahead: Int): String = when (ahead) {
    0 -> "¡Eres el siguiente!"
    1 -> "Falta 1 turno"
    else -> "Faltan $ahead turnos"
}
