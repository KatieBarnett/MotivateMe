package dev.motivateme.widget.quote

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.motivateme.MainViewModel
import dev.motivateme.R
import dev.motivateme.models.Quote
import dev.motivateme.models.QuoteWidgetState
import dev.motivateme.ui.screens.TopicScreen
import dev.motivateme.ui.theme.MotivateMeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuoteWidgetConfigurationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, just finish.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // If the user backs out of the activity before reaching the end, the system notifies the
        // app widget host that the configuration is canceled and the host doesn't add the widget
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            MotivateMeTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val topics by viewModel.topics.collectAsStateWithLifecycle()
                val generatedTopics by viewModel.generatedTopics.collectAsStateWithLifecycle()
                val loading by viewModel.showLoading.collectAsStateWithLifecycle()
                TopicScreen(
                    topics = topics + generatedTopics,
                    welcomeText = stringResource(R.string.widget_config_text),
                    loading = loading,
                    onTopicClick = { topicName ->
                        // Save the widget state here
                        val context = this@QuoteWidgetConfigurationActivity
                        coroutineScope.launch {
                            val quote = async(Dispatchers.IO) {
                                viewModel.showLoading.emit(true)
                                viewModel.getSingleQuote(topicName)
                            }
                            val manager = GlanceAppWidgetManager(context)
                            val glanceId = manager.getGlanceIdBy(appWidgetId)
                            updateAppWidgetState(
                                context = context,
                                definition = QuoteWidgetStateDefinition,
                                glanceId = glanceId
                            ) { prefs ->
                                QuoteWidgetState.Available(
                                    topicName = topicName,
                                    quote = Quote(text = quote.await()?.text ?: "Quote not found")
                                )
                            }
                            QuoteWidget().update(context, glanceId)
                            viewModel.showLoading.emit(false)

                            val resultValue = Intent().putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId
                            )
                            setResult(RESULT_OK, resultValue)
                            finish()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
