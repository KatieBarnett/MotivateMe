package dev.motivateme.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("motivateme_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PINNED_PREFIX = "pinned_"
    }

    fun isWidgetPinned(topic: String): Boolean {
        return sharedPreferences.getBoolean(KEY_PINNED_PREFIX + topic, false)
    }

    fun setWidgetPinned(topic: String, pinned: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PINNED_PREFIX + topic, pinned).apply()
    }
}
