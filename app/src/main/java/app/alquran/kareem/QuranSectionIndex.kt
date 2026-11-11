package app.alquran.kareem

import android.content.res.AssetManager
import org.json.JSONObject

/** Offline section navigation only. Never writes or generates Quranic text. */
internal data class QuranSectionStart(
    val number: Int,
    val page: Int,
    val surah: Int,
    val ayah: Int
)

internal data class QuranSectionIndex(
    val juz: List<QuranSectionStart>,
    val hizb: List<QuranSectionStart>
)

internal object QuranSectionRules {
    fun isValid(items: List<QuranSectionStart>, expected: Int): Boolean {
        if (expected !in listOf(30, 60) || items.size != expected) return false
        var lastPage = 0
        var lastSurah = 0
        var lastAyah = 0
        for ((index, item) in items.withIndex()) {
            if (item.number != index + 1 ||
                item.page !in 1..604 || item.page < lastPage ||
                item.surah !in 1..114 || item.ayah !in 1..286) return false
            if (item.surah < lastSurah ||
                (item.surah == lastSurah && item.ayah < lastAyah)) return false
            lastPage = item.page
            lastSurah = item.surah
            lastAyah = item.ayah
        }
        if (items.first().page != 1) return false
        return expected != 30 || items.last().page == 582
    }
}

internal object QuranSectionStore {
    fun load(assets: AssetManager): QuranSectionIndex = try {
        val json = assets.open("metadata/sections.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        require(json.getInt("page_count") == 604)
        fun decode(key: String): List<QuranSectionStart> {
            val values = json.getJSONArray(key)
            return (0 until values.length()).map { index ->
                val item = values.getJSONObject(index)
                QuranSectionStart(
                    number = item.getInt("number"),
                    page = item.getInt("page"),
                    surah = item.getInt("surah"),
                    ayah = item.getInt("ayah")
                )
            }
        }
        val juz = decode("juz")
        val hizb = decode("hizb")
        require(QuranSectionRules.isValid(juz, 30))
        require(QuranSectionRules.isValid(hizb, 60))
        QuranSectionIndex(juz, hizb)
    } catch (_: Exception) {
        QuranSectionIndex(emptyList(), emptyList())
    }
}
