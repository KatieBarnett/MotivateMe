package dev.motivateme.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.runBlocking

// Create the GlanceAppWidgetReceiver here named QuoteWidgetReceiver

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget =
        QuoteWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            runBlocking {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                var topic = ""
                updateAppWidgetState(context, glanceId) { prefs ->
                    topic = prefs[QuoteWidget.KEY_TOPIC] ?: ""
                }
                QuoteWidgetWorker.enqueuePeriodicWork(context, appWidgetId, topic)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            QuoteWidgetWorker.cancel(context, appWidgetId)
        }
    }
}