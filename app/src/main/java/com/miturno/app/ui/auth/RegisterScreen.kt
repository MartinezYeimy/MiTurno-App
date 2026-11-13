package com.miturno.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miturno.app.data.model.User
import com.miturno.app.ui.components.AppScaffold
import com.miturno.app.ui.components.AppTextField
import com.miturno.app.ui.components.BigButton
import com.miturno.app.ui.components.NoticeCard
import com.miturno.app.ui.theme.Ink

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onRegistered: (User) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var document by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var affiliation by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }

    val submit = {
        vm.register(name, document, email, affiliation, password, confirm, accepted) { user ->
            Toast.makeText(context, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
            onRegistered(user)
        }
    }

    AppScaffold(title = "Crear cuenta", onBack = onBack) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Llena tus datos tal como aparecen en tu carné de afiliación.",
                style = MaterialTheme.typography.bodyLarge, color = Ink
            )
            AppTextField(name, { name = it.take(60) }, "Nombre completo")
            AppTextField(document, { document = it.filter(Char::isDigit).take(15) }, "Documento de identidad", keyboardType = KeyboardType.Number)
            AppTextField(email, { email = it.take(50) }, "Correo electrónico", keyboardType = KeyboardType.Email)
            AppTextField(affiliation, { affiliation = it.take(20) }, "Número de afiliación")
            AppTextField(password, { password = it.take(20) }, "Contraseña (mínimo 6 caracteres)", isPassword = true)
            AppTextField(confirm, { confirm = it.take(20) }, "Repite la contraseña", isPassword = true, imeAction = ImeAction.Done, onDone = { submit() })

            // Ley 1581 de 2012 (RNF-04)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { accepted = !accepted },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text(
                    "Autorizo el tratamiento de mis datos personales conforme a la Ley 1581 de 2012.",
                    style = MaterialTheme.typography.bodyMedium, color = Ink
                )
            }

            ui.error?.let { NoticeCard(it, isError = true) }

            if (ui.loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                BigButton("Crear cuenta", onClick = { submit() })
            }
        }
    }
}
