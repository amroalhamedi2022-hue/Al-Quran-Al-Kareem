package app.alquran.kareem

import android.content.Context

/**
 * Entirely offline, user-controlled page completion marks. Private preferences
 * contain only page numbers, never source text or images, and never sync.
 */
internal class KhatmaProgressStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "local_quran_khatma_marked_pages_v1", Context.MODE_PRIVATE
    )

    fun read(totalPages: Int): Set<Int> {
        val stored = preferences.getStringSet("marked_pages", emptySet()).orEmpty()
        return KhatmaProgressRules.sanitize(stored.mapNotNull { it.toIntOrNull() }, totalPages)
    }

    fun save(pages: Set<Int>, totalPages: Int) {
        val clean = KhatmaProgressRules.sanitize(pages, totalPages)
        preferences.edit().putStringSet("marked_pages", clean.map(Int::toString).toSet()).apply()
    }

    fun clear() {
        preferences.edit().remove("marked_pages").apply()
    }
}
