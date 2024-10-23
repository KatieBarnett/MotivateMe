package dev.motivateme.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.motivateme.models.Quote
import dev.motivateme.models.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val GENERATED_TOPICS_KEY = stringPreferencesKey("generatedTopics")
        private const val TOPICS_DATASTORE = "topics_datastore"
        private const val SPLITTING_DELIMITER = "::"
    }

    private val Context.preferenceDataStore: DataStore<Preferences> by preferencesDataStore(name = TOPICS_DATASTORE)

    fun hasGeneratedTopics(): Flow<Boolean> {
        return context.preferenceDataStore.data.map { preferences ->
            preferences.contains(GENERATED_TOPICS_KEY)
        }
    }

    fun getTopics(): Flow<List<Topic>>{
        return flowOf(sampleData)
    }

    fun getGeneratedTopics(): Flow<List<Topic>> {
        return context.preferenceDataStore.data.map { preferences ->
            preferences[GENERATED_TOPICS_KEY]?.split(SPLITTING_DELIMITER)?.map {
                Topic(
                    name = it.trim(),
                    isGenerative = true,
                    quotes = emptyList()
                )
            } ?: emptyList()
        }
    }

    suspend fun saveGeneratedTopics(topic: List<Topic>) {
        context.preferenceDataStore.edit { preferences ->
            preferences[GENERATED_TOPICS_KEY] = topic.joinToString(SPLITTING_DELIMITER) { it.name }
        }
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