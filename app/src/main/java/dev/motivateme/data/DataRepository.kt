package dev.motivateme.data

import dev.motivateme.models.Quote
import dev.motivateme.models.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor() {

    fun getTopics(): Flow<List<Topic>> {
        return flowOf(sampleData)
    }
}

val sampleData = listOf(
    Topic(
        name = "Brain Boosters",
        quotes = listOf(
            Quote(text = "Knowledge is power, but ignorance is bliss."),
            Quote(text = "Learning is like a garden, you reap what you sow."),
            Quote(text = "Don't just be a bookworm, be a knowledge seeker.")
        )
    ),
    Topic(
        name = "Veggie Vibes",
        quotes = listOf(
            Quote(text = "Eat your greens, they're the spinach of your life."),
            Quote(text = "Carrots aren't just for bunnies, they're for humans too!"),
            Quote(text = "Broccoli: the tree of life for your plate.")
        )
    ),
    Topic(
        name = "Penny Pinching",
        quotes = listOf(
            Quote(text = "Save your pennies, so you can spend your dollars."),
            Quote(text = "Money doesn't grow on trees, but it can grow in your wallet."),
            Quote(text = "Don't be a piggy bank, be a money saver.")
        )
    )
)