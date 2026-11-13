package com.miturno.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miturno.app.data.model.Role
import com.miturno.app.data.model.User
import com.miturno.app.ui.components.AppTextField
import com.miturno.app.ui.components.BigButton
import com.miturno.app.ui.components.ButtonKind
import com.miturno.app.ui.components.NoticeCard
import com.miturno.app.ui.theme.Blue700
import com.miturno.app.ui.theme.Blue900
import com.miturno.app.ui.theme.Ink

//página de inicio de sesión
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onRegister: () -> Unit,
    onLoggedIn: (User) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var role by remember { mutableStateOf(Role.AFFILIATE) }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isStaff = role == Role.STAFF

    val submit = {
        vm.login(identifier.take(30), password.take(20), role) { user ->
            Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            onLoggedIn(user)
        }
    }

    Scaffold(containerColor = Color.White) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado con la marca
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(88.dp).background(Blue700, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(52.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("MiTurno", style = MaterialTheme.typography.headlineLarge, color = Blue900)
                Text(
                    "Sigue tu turno desde tu celular, sin hacer fila.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    textAlign = TextAlign.Center
                )
            }

            // Selector de perfil
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                RoleButton("Soy afiliado", !isStaff, Modifier.weight(1f)) { role = Role.AFFILIATE; vm.clearError() }
                RoleButton("Soy funcionario", isStaff, Modifier.weight(1f)) { role = Role.STAFF; vm.clearError() }
            }

            AppTextField(
                value = identifier,
                onValueChange = { identifier = it.take(30) },
                label = if (isStaff) "Usuario institucional" else "Documento, correo o N.º de afiliación",
                keyboardType = if (isStaff) KeyboardType.Text else KeyboardType.Email
            )
            AppTextField(
                value = password,
                onValueChange = { password = it.take(20) },
                label = "Contraseña",
                isPassword = true,
                imeAction = ImeAction.Done,
                onDone = { submit() }
            )

            ui.error?.let { NoticeCard(it, isError = true) }

            if (ui.loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                BigButton("Iniciar sesión", onClick = { submit() })
            }
5
            if (!isStaff) {
                TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("¿No tienes cuenta? Regístrate aquí")
                }
            }
        }
    }
}

@Composable
private fun RoleButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    BigButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        kind = if (selected) ButtonKind.Primary else ButtonKind.Secondary
    )
}