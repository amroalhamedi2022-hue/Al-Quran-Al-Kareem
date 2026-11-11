package app.alquran.kareem

/**
 * The two-page book is laid out LEFT-to-RIGHT on screen as [left, right],
 * though Quran page numbers progress RIGHT-to-LEFT.
 *
 * Double-tapping either page must zoom the sheet actually touched, not
 * arbitrarily the first page number of the spread.
 */
internal object ReaderSpreadTapRules {
    fun tappedPage(tapX: Float, width: Float, visible: List<Int>): Int? {
        if (!tapX.isFinite() || !width.isFinite() || width <= 0f ||
            tapX < 0f || tapX > width || visible.size !in 1..2 ||
            visible.any { it !in 1..604 } || visible.toSet().size != visible.size
        ) return null

        return when (visible.size) {
            1 -> visible.first()
            else -> if (tapX < width / 2f) visible[0] else visible[1]
        }
    }
}
