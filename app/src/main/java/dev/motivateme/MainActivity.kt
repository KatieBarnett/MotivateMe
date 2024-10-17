package dev.motivateme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import dev.motivateme.ui.screens.QuoteScreen
import dev.motivateme.ui.screens.TopicScreen
import dev.motivateme.ui.theme.MotivateMeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotivateMeTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = hiltViewModel()
                NavHost(
                    navController = navController,
                    startDestination = Home,
                ) {
                    composable<Home> {
                        val topics by viewModel.topics.collectAsStateWithLifecycle()
                        TopicScreen(
                            topics = topics,
                            onTopicClick = { topicName ->
                                navController.navigate(QuotesDestination(topicName))
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable<QuotesDestination> { backStackEntry ->
                        val quoteDestination: QuotesDestination = backStackEntry.toRoute()
                        val topicName = quoteDestination.topicName
                        val quotes = viewModel.getQuotes(topicName)
                        QuoteScreen(
                            topicName = topicName,
                            quotes = quotes,
                            onNavigateBack = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}