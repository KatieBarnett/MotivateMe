package dev.motivateme.widget.image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.motivateme.MainActivity
import dev.motivateme.R
import dev.motivateme.models.ImageWidgetState
import dev.motivateme.widget.theme.MotivateMeGlancePreviewTheme
import dev.motivateme.widget.theme.MotivateMeGlanceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ImageWidget : GlanceAppWidget(errorUiLayout = R.layout.widget_error_layout) {

    override val stateDefinition = ImageWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long
        // running operations.

        provideContent {
            // UI code here
            val appContext = LocalContext.current
            MotivateMeGlanceTheme(appContext) { _ ->
                when (val widgetState = currentState<ImageWidgetState>()) {
                    is ImageWidgetState.Available -> {
                        ImageWidgetLayout(
                            imageContent = { modifier ->
                                if (widgetState.downloadedImageFilePath != null) {
                                    ImageWidgetUrlFile(
                                        downloadedImageFilePath = widgetState.downloadedImageFilePath,
                                        modifier = modifier
                                    )
                                } else if (widgetState.imageUrl != null) {
                                    ImageWidgetUrlBackgroundThread(
                                        widgetState.imageUrl,
                                        modifier
                                    )
                                } else {
                                    ImageWidgetPhoto(modifier)
                                }
                            },
                            imageAuthor = widgetState.imageAuthor,
                            modifier = GlanceModifier.appWidgetBackground().fillMaxSize()
                        )
                    }
                    ImageWidgetState.Loading -> ImageWidgetLoading()
                    is ImageWidgetState.Unavailable -> ImageWidgetError(
                        message = widgetState.message,
                    )
                }
            }
        }
    }
}

@Composable
fun ImageWidgetLayout(
    imageContent: @Composable (GlanceModifier) -> Unit,
    imageAuthor: String? = null,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageContent.invoke(
            GlanceModifier
                .defaultWeight()
                .fillMaxWidth()
                .cornerRadius(10.dp)
        )
        imageAuthor?.let {
            Text("Image by: $it", style = TextStyle(GlanceTheme.colors.primary))
        }
    }
}

@Composable
fun ImageWidgetDrawableIcon(
    modifier: GlanceModifier = GlanceModifier,
) {
    // Image from a photo drawable
    Image(
        provider = ImageProvider(R.drawable.ic_launcher_foreground_mono),
        contentDescription = "My image",
        colorFilter = ColorFilter.tint(
            GlanceTheme.colors.primary
        ),
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
fun ImageWidgetPhoto(
    modifier: GlanceModifier = GlanceModifier,
) {
    // Image from a photo drawable
    Image(
        provider = ImageProvider(R.drawable.mountain),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
fun ImageWidgetUrlBackgroundThread(
    imageUrl: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    // Image from a url fetched in a background thread
    val context = LocalContext.current
    var loadedBitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context).data(imageUrl).apply {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }.build()

            // Request the image to be loaded and return null if an error has occurred
            loadedBitmap = when (val result = context.imageLoader.execute(request)) {
                is ErrorResult -> null
                is SuccessResult -> result.drawable.toBitmapOrNull()
            }
        }
    }

    loadedBitmap.let { bitmap ->
        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        } else {
            CircularProgressIndicator(modifier)
        }
    }
}


@Composable
fun ImageWidgetUrlFile(
    downloadedImageFilePath: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    // Image from a url downloaded to a file in a Coroutine Worker
    val context = LocalContext.current
    val bitmap = if (downloadedImageFilePath.startsWith("content://")) {
        MediaStore.Images.Media.getBitmap(
            context.contentResolver,
            downloadedImageFilePath.toUri()
        )
    } else {
        BitmapFactory.decodeFile(downloadedImageFilePath)
    }
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
fun ImageWidgetLoading(
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity(intent))
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(
                GlanceTheme.colors.primary
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
    }
}

@Composable
fun ImageWidgetError(
    message: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .appWidgetBackground()
            .clickable(actionStartActivity(intent))
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(10.dp),
    ) {
        Text(
            text = message,
            style = TextStyle(
                GlanceTheme.colors.primary
            ),
            modifier = GlanceModifier.padding(8.dp)
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview(heightDp = 200, widthDp = 400)
fun ImageWidgetContentNoAuthorPreview() {
    MotivateMeGlancePreviewTheme(false) { _ ->
        ImageWidgetLayout(
            imageContent = { modifier ->
                ImageWidgetPhoto(modifier)
            },
            imageAuthor = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview(heightDp = 200, widthDp = 400)
fun ImageWidgetContentWithAuthorPreview() {
    MotivateMeGlancePreviewTheme(false) { _ ->
        ImageWidgetLayout(
            imageContent = { modifier ->
                ImageWidgetPhoto(modifier)
            },
            imageAuthor = "Anonymous",
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun ImageWidgetLoadingPreview() {
    MotivateMeGlancePreviewTheme(false) { _ ->
        ImageWidgetLoading()
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun ImageWidgetErrorPreview() {
    MotivateMeGlancePreviewTheme(false) { _ ->
        ImageWidgetError("Error message!")
    }
}

