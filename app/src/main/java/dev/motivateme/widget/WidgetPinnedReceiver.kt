package dev.motivateme.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.AndroidEntryPoint
import dev.motivateme.data.SharedPreferencesManager
import dev.motivateme.models.Quote
import dev.motivateme.models.WidgetState
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WidgetPinnedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        val topicName = intent.getStringExtra(EXTRA_TOPIC_NAME)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        Log.i("WidgetPinnedReceiver", "onReceive: topic=$topicName, appWidgetId=$appWidgetId")

        if (topicName != null) {
            sharedPreferencesManager.setWidgetPinned(topicName, true)

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                runBlocking {
                    try {
                        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                        updateAppWidgetState(
                            context = context,
                            definition = QuoteWidgetStateDefinition,
                            glanceId = glanceId
                        ) {
                            WidgetState.Loading
                        }
                        QuoteWidget().update(context, glanceId)
                        Log.i("WidgetPinnedReceiver", "Updated glance state for $appWidgetId")
                        QuoteWidgetWorker.enqueueOneTimeWork(context, appWidgetId, topicName)
                        QuoteWidgetWorker.enqueuePeriodicWork(context, appWidgetId, topicName, force = true)
                    } catch (e: Exception) {
                        Log.e("WidgetPinnedReceiver", "Error updating glance state", e)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_TOPIC_NAME = "extra_topic_name"
    }
}
