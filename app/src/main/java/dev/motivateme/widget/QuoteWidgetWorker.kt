package dev.motivateme.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.motivateme.data.GeminiInterface
import dev.motivateme.data.sampleData
import dev.motivateme.models.Quote
import dev.motivateme.models.WidgetState
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
            val currentState: WidgetState =
                getAppWidgetState(context, QuoteWidgetStateDefinition, glanceId)
            if (currentState is WidgetState.Available) {
                updateAppWidgetState(
                    context = context,
                    definition = QuoteWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = {
                        WidgetState.Loading
                    }
                )
                QuoteWidget().update(context, glanceId)

                // Note - if BuildConfig.IS_GEMINI_ENABLED is false here `getSingleQuote` will return null
                // and we wouldn't be accessing a generated topic here anyway so we don't need to check
                // the value of the setting
                // Do the work to generate the quote
                val generatedQuote = geminiInterface.getSingleQuote(currentState.topicName)
                val newQuote = sampleData.firstOrNull {
                    it.name == currentState.topicName
                }?.quotes?.random() ?: generatedQuote

                // Update the widget with this new state
                updateAppWidgetState(
                    context = context,
                    definition = QuoteWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = {
                        if (newQuote != null) {
                            WidgetState.Available(
                                topicName = currentState.topicName,
                                quote = Quote(text = newQuote.text)
                            )
                        } else {
                            WidgetState.Unavailable(message = "Quote not found")
                        }
                    }
                )
                // Let the widget know there is a new state so it updates the UI
                QuoteWidget().update(context, glanceId)
            }
        }
        return Result.success()
    }
}
