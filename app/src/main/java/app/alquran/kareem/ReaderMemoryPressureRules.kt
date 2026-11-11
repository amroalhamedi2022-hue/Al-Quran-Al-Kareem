package app.alquran.kareem

/**
 * Android ComponentCallbacks2 memory-pressure thresholds.
 *
 * UI_HIDDEN=20 and RUNNING_LOW=10 both require dropping the bounded bitmap
 * cache. We deliberately do NOT recycle any bitmaps that Compose may still
 * be presenting; LruCache eviction alone releases the extra reference.
 */
internal object ReaderMemoryPressureRules {
    const val RUNNING_MODERATE = 5
    const val RUNNING_LOW = 10

    fun shouldTrim(level: Int): Boolean = level >= RUNNING_LOW
}
