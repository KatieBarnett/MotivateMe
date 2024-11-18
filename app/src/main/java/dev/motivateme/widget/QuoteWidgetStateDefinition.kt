package dev.motivateme.widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import dev.motivateme.models.WidgetState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.readBytes
import kotlin.io.use
import kotlin.text.decodeToString
import kotlin.text.encodeToByteArray
import kotlin.text.lowercase

object QuoteWidgetStateDefinition : GlanceStateDefinition<WidgetState> {

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
    object WidgetStateSerializer : Serializer<WidgetState> {
        override val defaultValue = WidgetState.Unavailable("Quote not found")

        override suspend fun readFrom(input: InputStream): WidgetState = try {
            Json.decodeFromString(
                WidgetState.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read widget state: ${exception.message}")
        }

        override suspend fun writeTo(t: WidgetState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(WidgetState.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}