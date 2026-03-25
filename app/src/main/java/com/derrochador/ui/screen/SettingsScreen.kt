package com.derrochador.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isModelReady: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var watcherEnabled by remember { mutableStateOf(true) }
    var visionEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // AI Model Status
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isModelReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isModelReady) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Modelo de IA local",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (isModelReady) "Gemma 2B activo"
                            else "No disponible — modo básico activo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Watcher Agent toggle
            ListItem(
                headlineContent = { Text("Agente Oídos") },
                supportingContent = { Text("Monitorear notificaciones bancarias") },
                trailingContent = {
                    Switch(
                        checked = watcherEnabled,
                        onCheckedChange = { watcherEnabled = it }
                    )
                }
            )

            // Vision Agent toggle
            ListItem(
                headlineContent = { Text("Agente Ojos") },
                supportingContent = { Text("Detectar capturas de WhatsApp") },
                trailingContent = {
                    Switch(
                        checked = visionEnabled,
                        onCheckedChange = { visionEnabled = it }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Permisos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Notification access button
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acceso a notificaciones")
            }

            // App permissions button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Permisos de la aplicación")
            }
        }
    }
}
