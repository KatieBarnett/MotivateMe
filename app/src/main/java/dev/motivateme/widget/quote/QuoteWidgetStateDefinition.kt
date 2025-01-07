package dev.motivateme.widget.quote

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import dev.motivateme.models.QuoteWidgetState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object QuoteWidgetStateDefinition : GlanceStateDefinition<QuoteWidgetState> {

    private const val DATA_STORE_FILENAME_PREFIX = "quote_widget_state_"

    /**
     * Use the same file name regardless of the widget instance to share data between them
     * If you need different state/data for each instance, create a store using the provided fileKey
     */
    override suspend fun getDataStore(context: Context, fileKey: String) = DataStoreFactory.create(
        serializer = WidgetStateSerializer,
        produceFile = { getLocation(context, fileKey) }
    )

    override fun getLocation(context: Context, fileKey: String) =
        context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey.lowercase())

    // Custom serializer
    object WidgetStateSerializer : Serializer<QuoteWidgetState> {
        override val defaultValue = QuoteWidgetState.Unavailable("Quote not found")

        override suspend fun readFrom(input: InputStream): QuoteWidgetState = try {
            Json.decodeFromString(
                QuoteWidgetState.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read widget state: ${exception.message}")
        }

        override suspend fun writeTo(t: QuoteWidgetState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(QuoteWidgetState.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}