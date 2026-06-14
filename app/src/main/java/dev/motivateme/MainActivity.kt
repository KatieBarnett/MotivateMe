package dev.motivateme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.motivateme.ui.screens.QuoteScreen
import dev.motivateme.ui.screens.TopicScreen
import dev.motivateme.ui.theme.MotivateMeTheme
import dev.motivateme.widget.QuoteWidget

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotivateMeTheme {
                val backStack = rememberNavBackStack(Home as NavKey)
                val viewModel: MainViewModel = hiltViewModel()
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key: NavKey ->
                        when (key) {
                            is Home -> NavEntry(key) {
                                val topics by viewModel.topics.collectAsStateWithLifecycle()
                                TopicScreen(
                                    topics = topics,
                                    onTopicClick = { topicName ->
                                        backStack.add(QuotesDestination(topicName))
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            is QuotesDestination -> NavEntry(key) {
                                val topicName = key.topicName
                                val quotes = viewModel.getQuotes(topicName)
                                QuoteScreen(
                                    topicName = topicName,
                                    quotes = quotes,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> error("Unknown key: $key")
                        }
                    }
                )
            }
            LaunchedEffect(Unit) {
                QuoteWidget().updateAll(this@MainActivity)
            }
        }
    }
}
