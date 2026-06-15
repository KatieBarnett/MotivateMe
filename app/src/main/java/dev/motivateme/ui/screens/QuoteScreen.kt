package dev.motivateme.ui.screens

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.motivateme.MainViewModel
import dev.motivateme.R
import dev.motivateme.data.sampleData
import dev.motivateme.models.Quote
import dev.motivateme.models.WidgetState
import dev.motivateme.ui.components.NavigationIcon
import dev.motivateme.ui.components.TopAppBarTitle
import dev.motivateme.ui.theme.MotivateMeTheme
import dev.motivateme.widget.QuoteWidget
import dev.motivateme.widget.QuoteWidgetReceiver
import dev.motivateme.widget.QuoteWidgetStateDefinition
import dev.motivateme.widget.WidgetPinnedReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    topicName: String,
    quotes: List<Quote>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val viewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current
    val isPinned = viewModel.isWidgetPinned(topicName)

    LaunchedEffect(topicName) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager.isRequestPinAppWidgetSupported && !isPinned) {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(QuoteWidget::class.java)
            var topicAlreadyExists = false
            for (glanceId in glanceIds) {
                val state = getAppWidgetState<WidgetState>(context, QuoteWidgetStateDefinition, glanceId)
                if (state is WidgetState.Available && state.topicName == topicName) {
                    topicAlreadyExists = true
                    break
                }
            }

            if (!topicAlreadyExists) {
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    topicName.hashCode(),
                    Intent(context, WidgetPinnedReceiver::class.java).apply {
                        putExtra(WidgetPinnedReceiver.EXTRA_TOPIC_NAME, topicName)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val widgetProvider = ComponentName(context, QuoteWidgetReceiver::class.java)
                appWidgetManager.requestPinAppWidget(widgetProvider, null, successCallback)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { TopAppBarTitle("${stringResource(R.string.app_name)}: $topicName") },
                navigationIcon = { NavigationIcon(onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(quotes) { quote ->
                QuoteBlock(
                    quote = quote,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun QuoteBlock(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors()
    ) {
        Text(
            text = quote.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteScreenPreview() {
    MotivateMeTheme {
        QuoteScreen(
            topicName = sampleData.first().name,
            quotes = sampleData.first().quotes,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteBlockPreview() {
    MotivateMeTheme {
        QuoteBlock(sampleData.first().quotes.first())
    }
}
