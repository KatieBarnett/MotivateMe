package dev.motivateme.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
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
import androidx.glance.unit.ColorProvider
import dev.motivateme.MainActivity
import dev.motivateme.R
import dev.motivateme.models.WidgetState
import dev.motivateme.widget.theme.MotivateMeGlancePreviewTheme
import dev.motivateme.widget.theme.MotivateMeGlanceTheme

class QuoteWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override val stateDefinition = QuoteWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val appContext = LocalContext.current
            MotivateMeGlanceTheme(appContext) { useDarkColorOnWallPaper ->
                val widgetState = currentState<WidgetState>()
                when (widgetState) {
                    is WidgetState.Available -> QuoteWidgetContent(
                        displayText = widgetState.quote.text,
                        topic = widgetState.topicName,
                        useDarkColorOnWallPaper = useDarkColorOnWallPaper
                    )
                    WidgetState.Loading -> QuoteWidgetLoading(useDarkColorOnWallPaper)
                    is WidgetState.Unavailable -> QuoteWidgetError(
                        message = widgetState.message,
                        useDarkColorOnWallPaper = useDarkColorOnWallPaper
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteWidgetContent(
    displayText: String,
    topic: String,
    useDarkColorOnWallPaper: Boolean,
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
            // .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                // color = GlanceTheme.colors.primary
                color = if (useDarkColorOnWallPaper) {
                    ColorProvider(Color.Black)
                } else {
                    ColorProvider(Color.White)
                }
            ),
            modifier = GlanceModifier.padding(8.dp).padding(end = 16.dp)
        )

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = modifier.fillMaxSize()
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground_mono),
                contentDescription = "Update",
                // colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                colorFilter = ColorFilter.tint(
                    GlanceTheme.colors.primary
                ),
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

@Composable
fun QuoteWidgetLoading(
    useDarkColorOnWallPaper: Boolean,
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
            // .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(
                if (useDarkColorOnWallPaper) {
                    ColorProvider(Color.Black)
                } else {
                    ColorProvider(Color.White)
                }
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
    }
}

@Composable
fun QuoteWidgetError(
    message: String,
    useDarkColorOnWallPaper: Boolean,
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
            // .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = message,
            style = TextStyle(
                if (useDarkColorOnWallPaper) {
                    ColorProvider(Color.Black)
                } else {
                    ColorProvider(Color.White)
                }
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
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
        updateAppWidgetState(
            context = context,
            definition = QuoteWidgetStateDefinition,
            glanceId = glanceId
        ) { prefs ->
            WidgetState.Loading
        }
        QuoteWidget().update(context, glanceId)

        val currentTopicName = parameters[topicKey]
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        QuoteWidgetWorker.enqueueOneTimeWork(context, appWidgetId, currentTopicName ?: "")
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetContentPreview() {
    MotivateMeGlancePreviewTheme(false) { useDarkColorOnWallPaper ->
        QuoteWidgetContent("Hello widget!", "Topic", useDarkColorOnWallPaper)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetLoadingPreview() {
    MotivateMeGlancePreviewTheme(false) { useDarkColorOnWallPaper ->
        QuoteWidgetLoading(useDarkColorOnWallPaper)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun QuoteWidgetErrorPreview() {
    MotivateMeGlancePreviewTheme(false) { useDarkColorOnWallPaper ->
        QuoteWidgetError("Error message!", useDarkColorOnWallPaper)
    }
}
