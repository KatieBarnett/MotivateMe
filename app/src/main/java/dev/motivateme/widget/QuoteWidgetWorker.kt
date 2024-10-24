package dev.motivateme.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.motivateme.data.GeminiInterface
import dev.motivateme.data.sampleData
import dev.motivateme.widget.QuoteWidget.Companion.KEY_QUOTE
import dev.motivateme.widget.QuoteWidget.Companion.KEY_TOPIC
import java.util.concurrent.TimeUnit

class QuoteWidgetWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {

        // We will use this worker to update all the widgets of this type at once, so our unique name
        // is just the class name. If you want to have a different worker for each individual widget
        // then you need to specify a unique name per widget
        private val uniqueWorkName = QuoteWidgetWorker::class.java.simpleName

        fun enqueue(context: Context, force: Boolean = false) {
            val workManager = WorkManager.getInstance(context)
            val request =
                PeriodicWorkRequestBuilder<QuoteWidgetWorker>(15, TimeUnit.MINUTES).build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = uniqueWorkName,
                existingPeriodicWorkPolicy = if (force) {
                    ExistingPeriodicWorkPolicy.UPDATE
                } else {
                    ExistingPeriodicWorkPolicy.KEEP
                },
                request = request
            )
        }


        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val geminiInterface = GeminiInterface()
        val appWidgetManager = GlanceAppWidgetManager(context)
        // Fetch the current state of each widget, get the current topic, fetch a new quote and update the state
        appWidgetManager.getGlanceIds(QuoteWidget::class.java).forEach { glanceId ->
            val currentWidgetState =
                getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
            val currentTopicName = currentWidgetState[KEY_TOPIC]

            // Note - if BuildConfig.IS_GEMINI_ENABLED is false here `getSingleQuote` will return null
            // and we wouldn't be accessing a generated topic here anyway so we don't need to check
            // the value of the setting
            val generatedQuote = geminiInterface.getSingleQuote(currentTopicName)
            val newQuote = sampleData.firstOrNull {
                it.name == currentTopicName
            }?.quotes?.random() ?: generatedQuote

            // Update the widget with this new state
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[KEY_QUOTE] = newQuote?.text ?: "Quote not found"
            }
            // Let the widget know there is a new state so it updates the UI
            QuoteWidget().update(context, glanceId)
        }
        return Result.success()
    }
}