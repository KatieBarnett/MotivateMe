package dev.motivateme.widget.theme

import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_SYSTEM
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceTheme
import dev.motivateme.widget.getUseDarkColorOnWallPaper

// Create the GlanceTheme here
@Composable
fun MotivateMeGlanceTheme(
    context: Context,
    content: @Composable (Boolean) -> Unit,
) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    val colors = wallpaperManager.getWallpaperColors(FLAG_SYSTEM)
    var useDarkColorOnWallpaper by remember {
        mutableStateOf(
            getUseDarkColorOnWallPaper(colors, FLAG_SYSTEM) ?: false
        )
    }
    DisposableEffect(wallpaperManager) {
        val listener = WallpaperManager.OnColorsChangedListener { colors, type ->
            getUseDarkColorOnWallPaper(colors, type)?.let {
                useDarkColorOnWallpaper = it
            }
        }
        wallpaperManager.addOnColorsChangedListener(
            listener,
            Handler(Looper.getMainLooper())
        )
        onDispose {
            wallpaperManager.removeOnColorsChangedListener(listener)
        }
    }
    GlanceTheme(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors
        } else {
            MotivateMeGlanceColorScheme.colors
        }
    ) {
        content.invoke(useDarkColorOnWallpaper)
    }
}

@Composable
fun MotivateMeGlancePreviewTheme(
    useDarkColorOnWallpaper: Boolean,
    content: @Composable (Boolean) -> Unit,
) {
    GlanceTheme(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlanceTheme.colors
        } else {
            MotivateMeGlanceColorScheme.colors
        }
    ) {
        content.invoke(useDarkColorOnWallpaper)
    }
}
