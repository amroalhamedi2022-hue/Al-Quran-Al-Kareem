package app.alquran.kareem

import android.content.Context

/** Device-only last-read position; never uses accounts, sync or Internet. */
internal class ReaderPositionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "local_quran_reader_position_v1", Context.MODE_PRIVATE
    )
    fun read(totalPages: Int): Int =
        ReaderPagination.clamp(preferences.getInt("last_page", 1), totalPages)

    fun save(page: Int, totalPages: Int) {
        preferences.edit().putInt("last_page", ReaderPagination.clamp(page, totalPages)).apply()
    }
}
