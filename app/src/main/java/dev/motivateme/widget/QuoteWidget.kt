package dev.motivateme.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.motivateme.MainActivity
import dev.motivateme.R
import dev.motivateme.data.GeminiInterface
import dev.motivateme.data.sampleData
import dev.motivateme.widget.theme.MotivateMeGlanceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

// Create the GlanceAppWidget here named QuoteWidget

class QuoteWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

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
                val topic = currentState(KEY_TOPIC) ?: ""
                QuoteWidgetContent(displayText, topic)
            }
        }
    }
}

@Composable
fun QuoteWidgetContent(
    displayText: String,
    topic: String,
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
            ),
            modifier = GlanceModifier.padding(8.dp)
        )

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = modifier.fillMaxSize()
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground_mono),
                contentDescription = "Update",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier
                    .padding(4.dp)
                    .size(32.dp)
                    .clickable(
                        actionRunCallback<RefreshAction>(actionParametersOf(RefreshAction.topicKey to topic))
                    )
            )
        }
    }
}

class RefreshAction : ActionCallback {

    companion object {
        val topicKey = ActionParameters.Key<String>("topic_param")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val currentTopicName = parameters[topicKey]

        // Note - if BuildConfig.IS_GEMINI_ENABLED is false here `getSingleQuote` will return null
        // and we wouldn't be accessing a generated topic here anyway so we don't need to check
        // the value of the setting
        val geminiInterface = GeminiInterface()
        val scope = CoroutineScope(Dispatchers.IO)
        val generatedQuote = scope.async(Dispatchers.IO) {
            geminiInterface.getSingleQuote(currentTopicName)
        }

        updateAppWidgetState(context, glanceId) { prefs ->
            val quote = sampleData.firstOrNull {
                it.name == currentTopicName
            }?.quotes?.random() ?: generatedQuote.await()
            prefs[QuoteWidget.KEY_QUOTE] = quote?.text ?: "Quote not found"
        }
        QuoteWidget().update(context, glanceId)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetContentPreview() {
    MotivateMeGlanceTheme {
        QuoteWidgetContent("Hello widget!", "Topic")
    }
}
