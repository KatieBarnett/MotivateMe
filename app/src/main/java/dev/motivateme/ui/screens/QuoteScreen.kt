package dev.motivateme.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.motivateme.R
import dev.motivateme.data.sampleData
import dev.motivateme.models.Quote
import dev.motivateme.ui.components.NavigationIcon
import dev.motivateme.ui.components.TopAppBarTitle
import dev.motivateme.ui.theme.MotivateMeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    topicName: String,
    quotes: List<Quote>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
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