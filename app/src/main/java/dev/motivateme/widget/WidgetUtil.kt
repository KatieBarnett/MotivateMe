package dev.motivateme.widget

import android.app.WallpaperColors
import android.app.WallpaperManager.FLAG_SYSTEM
import android.graphics.Color.RGBToHSV
import android.os.Build
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

fun getUseDarkColorOnWallPaper(colors: WallpaperColors?, type: Int): Boolean? {
    return if (type and FLAG_SYSTEM != 0 && colors != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (colors.colorHints) and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0
        } else {
            val hsv = FloatArray(3)
            val primaryColor = colors.primaryColor.toArgb()
            RGBToHSV(
                primaryColor.red,
                primaryColor.green,
                primaryColor.blue,
                hsv
            )
            !colorIsDarkAdvanced(primaryColor)
        }
    } else {
        null
    }
}

// https://stackoverflow.com/questions/3942878/how-to-decide-font-color-in-white-or-black-depending-on-background-color
fun colorIsDarkAdvanced(bgColor: Int): Boolean {
    // hexToB
    val uicolors = doubleArrayOf(
        bgColor.red.toDouble() / 255.0,
        bgColor.green.toDouble() / 255.0,
        bgColor.blue.toDouble() / 255.0
    )
    val c = uicolors.map { col ->
        if (col <= 0.03928) {
            col / 12.92
        } else {
            Math.pow((col + 0.055) / 1.055, 2.4)
        }
    }
    val L = 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]
    return L <= 0.179
}