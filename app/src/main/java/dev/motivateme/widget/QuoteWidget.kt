package dev.motivateme.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text

// Create the GlanceAppWidget here named QuoteWidget

class QuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long
        // running operations.

        val displayText = "Hello widget!"

        provideContent {
            // UI code here
            GlanceTheme {
                QuoteWidgetContent(displayText)
            }
        }
    }
}

@Composable
fun QuoteWidgetContent(
    displayText: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .appWidgetBackground(),
    ) {
        Text(displayText)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetContentPreview() {
    GlanceTheme {
        QuoteWidgetContent("Hello widget!")
    }
}

