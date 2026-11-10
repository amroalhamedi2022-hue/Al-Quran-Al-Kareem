package app.alquran.kareem

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Read-only, bounded page bitmap cache. Only approved page assets are reachable:
 * this class is constructed exclusively from the Approved reader branch.
 *
 * Cache eviction does not call recycle(): a bitmap can still be displayed by
 * Compose while a different page replaces it in the cache.
 */
internal class QuranPageBitmapRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private val decodeMutex = Mutex()
    private val budgetBytes: Int = run {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // A 1600x2287 ARGB_8888 bitmap occupies ~14 MiB. Leave most of the heap
        // for UI and Android on low-memory devices; never cache a full Mushaf.
        QuranPageCachePolicy.budgetBytes(manager.memoryClass)
    }
    private val bitmapCache = object : LruCache<Int, Bitmap>(budgetBytes) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
    }

    suspend fun load(page: Int): Bitmap? {
        require(page in 1..604)
        synchronized(bitmapCache) { bitmapCache.get(page) }?.let { return it }
        return decodeMutex.withLock {
            synchronized(bitmapCache) { bitmapCache.get(page) }?.let { return@withLock it }
            val decoded = withContext(Dispatchers.IO) {
                try {
                    val name = "quran/%03d.png".format(Locale.ROOT, page)
                    val options = BitmapFactory.Options().apply {
                        inScaled = false
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inMutable = false
                    }
                    assets.open(name).use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                } catch (_: Exception) {
                    null // Fail closed; do not render replacement Quranic text.
                } catch (_: OutOfMemoryError) {
                    null // Never crash the reader or substitute fabricated text.
                }
            }
            if (decoded == null) return@withLock null
            if (!QuranPageCachePolicy.acceptsDimensions(decoded.width, decoded.height)) {
                decoded.recycle()
                return@withLock null
            }
            synchronized(bitmapCache) { bitmapCache.put(page, decoded) }
            decoded
        }
    }

    fun trim() {
        // Do not recycle evicted bitmaps; Compose can still hold a reference.
        synchronized(bitmapCache) { bitmapCache.evictAll() }
    }
}
