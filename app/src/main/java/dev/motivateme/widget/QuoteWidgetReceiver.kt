package dev.motivateme.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// Create the GlanceAppWidgetReceiver here named QuoteWidgetReceiver

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget =
        QuoteWidget()
}