package app.alquran.kareem

import android.content.res.AssetManager
import org.json.JSONObject

/** Verified Madinah page index; metadata only, NEVER Quran verse text. */
internal data class PageVerseAnchor(val page: Int, val surah: Int, val ayah: Int)

internal object VersePageRules {
    private fun beforeOrAt(aSurah: Int, aAyah: Int, bSurah: Int, bAyah: Int) =
        aSurah < bSurah || (aSurah == bSurah && aAyah <= bAyah)

    fun isComplete(items: List<PageVerseAnchor>): Boolean {
        if (items.size != 604 || items.first() != PageVerseAnchor(1, 1, 1) ||
            items.last() != PageVerseAnchor(604, 112, 1)) return false
        for ((i, item) in items.withIndex()) {
            if (item.page != i+1 || item.surah !in 1..114 || item.ayah !in 1..286) return false
            if (i > 0) {
                val previous = items[i-1]
                if (!beforeOrAt(previous.surah,previous.ayah,item.surah,item.ayah)) return false
            }
        }
        return true
    }

    fun findPage(items: List<PageVerseAnchor>, surah: Int, ayah: Int): Int? {
        if (items.isEmpty() || surah !in 1..114 || ayah !in 1..286) return null
        // Repeated first-verse markers may occur at a page boundary for a long
        // verse. Choose the first matching page; never invent missing text.
        items.firstOrNull { it.surah == surah && it.ayah == ayah }?.let { return it.page }
        var found: Int? = null
        for (entry in items) {
            if (!beforeOrAt(entry.surah, entry.ayah, surah, ayah)) break
            found = entry.page
        }
        return found
    }

    fun parseReference(input: String, surahs: List<SurahIndexEntry>): Pair<Int,Int>? {
        val parts = input.trim().replace('：',':').split(':')
        if (parts.size != 2 || surahs.size != 114) return null
        val chapter = ReaderNavigationRules.parsePage(parts[0],114) ?: return null
        val maximum = surahs[chapter-1].verses
        val verse = ReaderNavigationRules.parsePage(parts[1],maximum) ?: return null
        return chapter to verse
    }
}

internal object VersePageStore {
    fun load(assets: AssetManager): List<PageVerseAnchor> = try {
        val metadata = assets.open("metadata/verse_page_index.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        require(metadata.getInt("page_count") == 604)
        val raw = metadata.getJSONArray("first_verse_on_pages")
        val items = (0 until raw.length()).map { index ->
            val entry = raw.getJSONObject(index)
            PageVerseAnchor(
                page = entry.getInt("page"),
                surah = entry.getInt("surah"),
                ayah = entry.getInt("ayah")
            )
        }
        require(VersePageRules.isComplete(items))
        items
    } catch (_: Exception) {
        emptyList()
    }
}
