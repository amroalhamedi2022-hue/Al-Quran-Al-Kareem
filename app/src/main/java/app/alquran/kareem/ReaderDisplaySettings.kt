package app.alquran.kareem

import android.content.Context

/** Entirely local reading choices. No network, login or analytics. */
internal data class ReaderDisplaySettings(
    val keepScreenAwake: Boolean = false,
    val reduceMotion: Boolean = false
)

internal class ReaderDisplaySettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "quran_reading_display_settings_v1", Context.MODE_PRIVATE
    )

    fun read(): ReaderDisplaySettings = ReaderDisplaySettings(
        keepScreenAwake = prefs.getBoolean("keep_screen_awake", false),
        reduceMotion = prefs.getBoolean("reduce_motion", false)
    )

    fun save(settings: ReaderDisplaySettings) {
        prefs.edit()
            .putBoolean("keep_screen_awake", settings.keepScreenAwake)
            .putBoolean("reduce_motion", settings.reduceMotion)
            .apply()
    }
}

/** Pure motion rules: user accessibility choice takes precedence over effects. */
internal object ReaderMotionRules {
    fun animateTurn(reduceMotion: Boolean) = !reduceMotion
    fun turnDurationMs(reduceMotion: Boolean): Int =
        if (reduceMotion) 0 else 190
}
