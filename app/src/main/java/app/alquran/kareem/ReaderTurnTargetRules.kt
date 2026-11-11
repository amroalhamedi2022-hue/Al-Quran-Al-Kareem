package app.alquran.kareem

/**
 * Navigation is always calculated against the reader's normal book layout,
 * not the temporary single-page viewport used while a sheet is magnified.
 * Otherwise a tablet could "turn" from page 1 to page 2 and then display
 * the unchanged [2,1] spread when 100% zoom was restored.
 */
internal object ReaderTurnTargetRules {
    fun target(page: Int, totalPages: Int, normalTwoPageSpread: Boolean, forward: Boolean): Int =
        if (forward) ReaderPagination.next(page, totalPages, normalTwoPageSpread)
        else ReaderPagination.previous(page, totalPages, normalTwoPageSpread)
}
