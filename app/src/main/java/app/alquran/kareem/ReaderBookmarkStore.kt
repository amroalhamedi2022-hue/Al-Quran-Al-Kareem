package app.alquran.kareem

import android.content.Context

/**
 * Page-level bookmarks stored only on this device, without any network, account
 * or permission. Does not modify Quran images or the immutable source manifest.
 */
internal class ReaderBookmarkStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "local_quran_bookmarks_v1", Context.MODE_PRIVATE
    )

    fun read(pageCount: Int): Set<Int> {
        val saved = preferences.getStringSet("pages", emptySet()).orEmpty()
        val parsed = saved.mapNotNull { it.toIntOrNull() }
        return ReaderNavigationRules.sanitizedPages(parsed, pageCount)
    }

    fun save(pages: Set<Int>, pageCount: Int) {
        val clean = ReaderNavigationRules.sanitizedPages(pages, pageCount)
        preferences.edit().putStringSet("pages", clean.map(Int::toString).toSet()).apply()
    }
}
