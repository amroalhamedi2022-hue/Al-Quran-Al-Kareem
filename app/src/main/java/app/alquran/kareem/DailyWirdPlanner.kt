package app.alquran.kareem

import android.content.Context

/**
 * Optional manual daily reading target. No timestamps, automatic page-completion,
 * analytics, scoring or online features. The user alone marks pages read.
 */
internal object DailyWirdRules {
    const val MIN_PAGES = 1
    const val MAX_PAGES = 30
    const val DEFAULT_PAGES = 5

    fun normalizeTarget(requested: Int): Int =
        requested.coerceIn(MIN_PAGES, MAX_PAGES)

    fun suggestedNextPages(
        readPages: Set<Int>,
        totalPages: Int = 604,
        dailyTarget: Int = DEFAULT_PAGES
    ): List<Int> {
        require(totalPages > 0)
        val finished = KhatmaProgressRules.sanitize(readPages, totalPages)
        return (1..totalPages).asSequence()
            .filterNot { it in finished }
            .take(normalizeTarget(dailyTarget))
            .toList()
    }

    fun estimatedDaysRemaining(
        readPages: Set<Int>,
        totalPages: Int = 604,
        dailyTarget: Int = DEFAULT_PAGES
    ): Int {
        require(totalPages > 0)
        val left = totalPages - KhatmaProgressRules.sanitize(readPages, totalPages).size
        val target = normalizeTarget(dailyTarget)
        return (left + target - 1) / target
    }
}

internal class DailyWirdStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "quran_offline_manual_wird_v1", Context.MODE_PRIVATE
    )

    fun readTarget(): Int = DailyWirdRules.normalizeTarget(
        preferences.getInt("target_pages_per_day", DailyWirdRules.DEFAULT_PAGES)
    )

    fun saveTarget(value: Int) {
        preferences.edit().putInt(
            "target_pages_per_day", DailyWirdRules.normalizeTarget(value)
        ).apply()
    }
}
