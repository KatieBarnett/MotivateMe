package dev.motivateme.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.motivateme.data.QuoteDataSource
import dev.motivateme.models.WidgetState
import java.util.concurrent.TimeUnit

@HiltWorker
class QuoteWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val quoteDataSource: QuoteDataSource
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "QuoteWidgetWorker"
        const val APP_WIDGET_ID_EXTRA = "app_widget_id_extra"
        const val TOPIC_KEY_EXTRA = "topic_key_extra"

        fun enqueueOneTimeWork(context: Context, appWidgetId: Int, topic: String) {
            val workManager = WorkManager.getInstance(context)
            val inputData = Data.Builder()
                .putInt(APP_WIDGET_ID_EXTRA, appWidgetId)
                .putString(TOPIC_KEY_EXTRA, topic)
                .build()

            val uniqueWorkName = "${QuoteWidgetWorker::class.java.simpleName}-OneTime-$appWidgetId"

            val request = OneTimeWorkRequestBuilder<QuoteWidgetWorker>()
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun enqueuePeriodicWork(context: Context, appWidgetId: Int, topic: String, force: Boolean = false) {
            val workManager = WorkManager.getInstance(context)

            val inputData = Data.Builder()
                .putInt(APP_WIDGET_ID_EXTRA, appWidgetId)
                .putString(TOPIC_KEY_EXTRA, topic)
                .build()

            val uniqueWorkName = "${QuoteWidgetWorker::class.java.simpleName}-$appWidgetId"

            val request =
                PeriodicWorkRequestBuilder<QuoteWidgetWorker>(15, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .build()

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

        fun cancel(context: Context, appWidgetId: Int) {
            val uniqueWorkName = "${QuoteWidgetWorker::class.java.simpleName}-$appWidgetId"
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(context)

        val targetId = inputData.getInt(APP_WIDGET_ID_EXTRA, -1)
        val targetTopic = inputData.getString(TOPIC_KEY_EXTRA)

        if (targetId != -1) {
            val glanceId = appWidgetManager.getGlanceIdBy(targetId)
            val topicToUpdate = if (!targetTopic.isNullOrBlank()) {
                targetTopic
            } else {
                val currentState = getAppWidgetState<WidgetState>(context, QuoteWidgetStateDefinition, glanceId)
                if (currentState is WidgetState.Available) currentState.topicName else null
            }

            if (topicToUpdate != null) {
                updateWidget(glanceId, topicToUpdate)
            }
        } else {
            appWidgetManager.getGlanceIds(QuoteWidget::class.java).forEach { glanceId ->
                val currentState = getAppWidgetState(context, QuoteWidgetStateDefinition, glanceId)
                if (currentState is WidgetState.Available) {
                    updateWidget(glanceId, currentState.topicName)
                }
            }
        }
        return Result.success()
    }

    private suspend fun updateWidget(glanceId: GlanceId, topicName: String) {
        updateAppWidgetState(
            context = context,
            definition = QuoteWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { WidgetState.Loading }
        )
        QuoteWidget().update(context, glanceId)

        val newQuote = quoteDataSource.getQuote(topicName)

        updateAppWidgetState(
            context = context,
            definition = QuoteWidgetStateDefinition,
            glanceId = glanceId,
            updateState = {
                if (newQuote != null) {
                    WidgetState.Available(topicName = topicName, quote = newQuote)
                } else {
                    WidgetState.Unavailable(message = "Quote not found")
                }
            }
        )
        QuoteWidget().update(context, glanceId)
    }
}
