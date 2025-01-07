package dev.motivateme.widget.image

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider.getUriForFile
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.motivateme.models.ImageWidgetState
import dev.motivateme.network.PicsumService
import java.util.concurrent.TimeUnit

@HiltWorker
class ImageWidgetWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val picsumService: PicsumService,
) : CoroutineWorker(context, workerParams) {

    companion object {

        // We will use this worker to update all the widgets of this type at once, so our unique name
        // is just the class name. If you want to have a different worker for each individual widget
        // then you need to specify a unique name per widget
        private val uniqueWorkName = ImageWidgetWorker::class.java.simpleName

        fun enqueue(context: Context, force: Boolean = false) {
            val workManager = WorkManager.getInstance(context)
            val request =
                PeriodicWorkRequestBuilder<ImageWidgetWorker>(15, TimeUnit.MINUTES).build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = uniqueWorkName,
                existingPeriodicWorkPolicy = if (force) {
                    ExistingPeriodicWorkPolicy.UPDATE
                } else {
                    ExistingPeriodicWorkPolicy.KEEP
                },
                request = request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val appWidgetManager = GlanceAppWidgetManager(context)
        // Fetch the current state of each widget, get the current topic, fetch a new Image and update the state
        appWidgetManager.getGlanceIds(ImageWidget::class.java).forEach { glanceId ->
            val currentState: ImageWidgetState =
                getAppWidgetState(context, ImageWidgetStateDefinition, glanceId)
            if (currentState is ImageWidgetState.Unavailable) {
                updateAppWidgetState(
                    context = context,
                    definition = ImageWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = {
                        ImageWidgetState.Loading
                    }
                )
                ImageWidget().update(context, glanceId)
            }

            // Get a random image
            val imageId = 401
//            val imageId = 237
            try {
                // Do the work to get the data
                val response = picsumService.getImageInfo(imageId)
                val newState = if (response.isSuccessful) {
                    response.body()?.let { data ->
                        ImageWidgetState.Available(
                            imageUrl = "https://picsum.photos/id/$imageId/400/200.jpg",
                            imageAuthor = data.author,
                            downloadedImageFilePath = downloadImage(
                                "https://picsum.photos/id/$imageId/400/200.jpg",
                                context,
                                applicationContext,
                                false
                            )
                        )
                    }
                } else {
                    null
                } ?: ImageWidgetState.Unavailable("Error getting image info")

                // Update the widget with this new state
                updateAppWidgetState(
                    context = context,
                    definition = ImageWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = {
                        newState
                    }
                )
            } catch (exception: Exception) {
                Log.e("ImageWidgetWorker", "Error getting image info", exception)
                updateAppWidgetState(
                    context = context,
                    definition = ImageWidgetStateDefinition,
                    glanceId = glanceId,
                    updateState = {
                        ImageWidgetState.Unavailable(
                            message = "Error downloading image",
                        )
                    }
                )
            }
            // Let the widget know there is a new state so it updates the UI
            ImageWidget().update(context, glanceId)
        }
        return Result.success()
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun downloadImage(
        url: String,
        context: Context,
        applicationContext: Context,
        force: Boolean
    ): String {
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()

        // Request the image to be loaded and throw error if it failed
        with(context.imageLoader) {
            if (force) {
                diskCache?.remove(url)
                memoryCache?.remove(MemoryCache.Key(url))
            }
            val result = execute(request)
            if (result is ErrorResult) {
                throw result.throwable
            }
        }

        // Get the path of the loaded image from DiskCache.
        val path = context.imageLoader.diskCache?.openSnapshot(url)?.use { snapshot ->
            val imageFile = snapshot.data.toFile()

            // Use the FileProvider to create a content URI
            val contentUri = getUriForFile(
                context,
                "${applicationContext.packageName}.provider",
                imageFile,
            )

            // Find the current launcher every time to ensure it has read permissions
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
                )
            } else {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY,
                )
            }
            val launcherName = resolveInfo?.activityInfo?.packageName
            if (launcherName != null) {
                context.grantUriPermission(
                    launcherName,
                    contentUri,
                    FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
                )
            }

            // return the path
            contentUri.toString()
        }
        return requireNotNull(path) {
            "Couldn't find cached file"
        }
    }
}
