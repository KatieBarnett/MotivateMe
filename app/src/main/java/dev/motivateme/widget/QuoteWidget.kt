package dev.motivateme.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.motivateme.MainActivity
import dev.motivateme.widget.theme.MotivateMeGlanceTheme

// Create the GlanceAppWidget here named QuoteWidget

class QuoteWidget : GlanceAppWidget() {

    companion object {
        val KEY_TOPIC = stringPreferencesKey("topic")
        val KEY_QUOTE = stringPreferencesKey("quote")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long
        // running operations.

        provideContent {
            // UI code here
            MotivateMeGlanceTheme {
                val displayText = currentState(KEY_QUOTE) ?: "Quote not found"
                QuoteWidgetContent(displayText)
            }
        }
    }
}

@Composable
fun QuoteWidgetContent(
    displayText: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity(intent))
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                color = GlanceTheme.colors.primary
            )
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetContentPreview() {
    MotivateMeGlanceTheme {
        QuoteWidgetContent("Hello widget!")
    }
}

