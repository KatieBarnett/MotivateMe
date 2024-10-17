package dev.motivateme.widget

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.motivateme.MainViewModel
import dev.motivateme.R
import dev.motivateme.ui.screens.TopicScreen
import dev.motivateme.ui.theme.MotivateMeTheme

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
                TopicScreen(
                    topics = topics,
                    welcomeText = stringResource(R.string.widget_config_text),
                    onTopicClick = { topicName ->

                        // Save the widget state here

                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
