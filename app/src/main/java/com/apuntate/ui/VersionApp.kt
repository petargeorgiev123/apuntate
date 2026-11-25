package com.apuntate.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apuntate.BuildConfig

/** Texto de versión, p. ej. "v1.1" (o "v1.1 (debug)" en el APK de pruebas). */
val textoVersion: String =
    "v${BuildConfig.VERSION_NAME}" + if (BuildConfig.DEBUG) " (debug)" else ""

@Composable
fun VersionApp(modifier: Modifier = Modifier) {
    Text(
        text = textoVersion,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
