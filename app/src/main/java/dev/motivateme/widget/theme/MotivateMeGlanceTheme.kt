package dev.motivateme.widget.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme

// Create the GlanceTheme here
@Composable
fun MotivateMeGlanceTheme(
    content: @Composable () -> Unit,
) {
    GlanceTheme(
        colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors
        } else {
            MotivateMeGlanceColorScheme.colors
        },
        content = { content.invoke() }
    )
}
