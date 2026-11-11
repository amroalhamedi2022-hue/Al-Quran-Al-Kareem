package app.alquran.kareem

import android.content.res.AssetManager
import org.json.JSONObject

/**
 * Offline navigation facts only. Contains NO Quran verses or text-rendering data.
 * Surah page starts were independently cross-checked between three datasets.
 */
internal data class SurahIndexEntry(
    val number: Int,
    val name: String,
    val startPage: Int,
    val verses: Int
)

internal object SurahIndexRules {
    private val diacritics = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

    fun normalize(raw: String): String =
        raw.trim().replace(diacritics, "").replace("ـ", "")
            .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
            .replace("ٱ", "ا").replace("ة", "ه").replace("ى", "ي")
            .lowercase()

    fun search(entries: List<SurahIndexEntry>, query: String): List<SurahIndexEntry> {
        val token = normalize(query)
        if (token.isBlank()) return entries
        return entries.filter {
            normalize(it.name).contains(token) ||
                it.number.toString() == token ||
                it.startPage.toString() == token
        }
    }

    fun isComplete(entries: List<SurahIndexEntry>, pageCount: Int = 604): Boolean {
        if (entries.size != 114 || pageCount != 604) return false
        var lastStart = 0
        var verses = 0
        for ((index, surah) in entries.withIndex()) {
            if (surah.number != index + 1 ||
                surah.name.isBlank() || surah.verses !in 1..300 ||
                surah.startPage !in lastStart.coerceAtLeast(1)..pageCount) return false
            lastStart = surah.startPage
            verses += surah.verses
        }
        return entries.first().startPage == 1 &&
            entries[1].startPage == 2 &&
            entries[2].startPage == 50 &&
            entries.last().startPage == 604 &&
            verses == 6236
    }
}

internal object SurahIndexStore {
    fun load(assets: AssetManager): List<SurahIndexEntry> = try {
        val json = assets.open("metadata/surahs.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        require(json.getInt("page_count") == 604)
        require(json.getInt("chapter_count") == 114)
        require(json.getInt("verse_count") == 6236)
        val raw = json.getJSONArray("chapters")
        val chapters = (0 until raw.length()).map { idx ->
            val entry = raw.getJSONObject(idx)
            SurahIndexEntry(
                number = entry.getInt("id"),
                name = entry.getString("name"),
                startPage = entry.getInt("page"),
                verses = entry.getInt("verses")
            )
        }
        require(SurahIndexRules.isComplete(chapters))
        chapters
    } catch (_: Exception) {
        // Broken metadata cannot cause an inaccurate jump to a page.
        emptyList()
    }
}
