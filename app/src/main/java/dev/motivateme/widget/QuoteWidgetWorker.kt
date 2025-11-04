package dev.motivateme.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.motivateme.data.GeminiInterface
import dev.motivateme.widget.QuoteWidget.Companion.KEY_QUOTE

class QuoteWidgetWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val APP_WIDGET_ID_EXTRA = "app_widget_id_extra"
        const val TOPIC_KEY_EXTRA = "topic_key_extra"
    }

    override suspend fun doWork(): Result {
        val geminiInterface = GeminiInterface()
        val appWidgetId = inputData.getInt(APP_WIDGET_ID_EXTRA, -1)
        val currentTopicName = inputData.getString(TOPIC_KEY_EXTRA)

        if (appWidgetId == -1 || currentTopicName.isNullOrBlank()) {
            return Result.failure()
        }

        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        val generatedQuote = geminiInterface.getQuote(currentTopicName)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[KEY_QUOTE] = generatedQuote?.text ?: "Quote not found"
        }
        QuoteWidget().update(context, glanceId)

        return Result.success()
    }
}
