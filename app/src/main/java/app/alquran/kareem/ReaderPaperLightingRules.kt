package app.alquran.kareem

/**
 * Temporary page-edge shading for the existing 3D hinge, not a curved mesh.
 * Never reads or edits any Quran pixel. At rest all overlay alpha is exactly 0.
 */
internal object ReaderPaperLightingRules {
    data class Light(val bandFraction: Float, val shadowAlpha: Float, val highlightAlpha: Float)
    data class PaperBounds(val left: Float, val top: Float, val width: Float, val height: Float)

    fun light(dragPixels: Float, viewportWidthPixels: Float): Light {
        val progress = ReaderPaperTurnRules.pose(dragPixels, viewportWidthPixels)
            .elevationFraction
        if (progress <= 0f) return Light(0f, 0f, 0f)
        // The soft band widens as the imaginary sheet rises off the book.
        val raised = progress * (2f - progress)
        return Light(
            bandFraction = 0.06f + 0.08f * progress,
            shadowAlpha = 0.17f * raised,
            highlightAlpha = 0.065f * progress
        )
    }

    /** Match ContentScale.Fit used by ApprovedPage; no guesswork about page edges. */
    fun fittedBounds(viewportWidth: Float, viewportHeight: Float): PaperBounds? {
        if (!viewportWidth.isFinite() || !viewportHeight.isFinite() ||
            viewportWidth <= 0f || viewportHeight <= 0f
        ) return null
        val sourceWidth = QuranPageCachePolicy.PAGE_WIDTH.toFloat()
        val sourceHeight = QuranPageCachePolicy.PAGE_HEIGHT.toFloat()
        val fit = minOf(viewportWidth / sourceWidth, viewportHeight / sourceHeight)
        val width = sourceWidth * fit
        val height = sourceHeight * fit
        return PaperBounds(
            left = (viewportWidth - width) / 2f,
            top = (viewportHeight - height) / 2f,
            width = width,
            height = height
        )
    }
}
