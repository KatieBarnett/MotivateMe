package dev.motivateme.widget.theme

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme

// Create the GlanceTheme here
@Composable
fun MotivateMeGlanceTheme(
    content: @Composable () -> Unit,
) {
    GlanceTheme(
        MotivateMeGlanceColorScheme.colors,
        content = { content.invoke() }
    )
}
