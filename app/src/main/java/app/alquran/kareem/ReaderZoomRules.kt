package app.alquran.kareem

/**
 * Zoom/pan geometry applies only to the Compose drawing layer.
 * It NEVER resizes, crops, saves or transcodes original Quran source PNGs.
 */
internal object ReaderZoomRules {
    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 3f
    const val STEP = 0.5f

    data class Pan(val x: Float, val y: Float)

    fun normalize(scale: Float): Float =
        if (scale.isFinite()) scale.coerceIn(MIN_ZOOM, MAX_ZOOM) else MIN_ZOOM

    fun isZoomed(scale: Float) = normalize(scale) > MIN_ZOOM

    fun zoomIn(scale: Float): Float = normalize(normalize(scale) + STEP)
    fun zoomOut(scale: Float): Float = normalize(normalize(scale) - STEP)

    fun toggleDoubleTap(scale: Float): Float =
        if (isZoomed(scale)) MIN_ZOOM else 2f

    /**
     * The source page is displayed with ContentScale.Fit, not FillBounds.
     * Its painted rectangle can be significantly narrower than the viewport
     * in landscape. Panning against full viewport dimensions used to expose
     * large empty areas, which was especially noticeable on a tablet.
     */
    fun clampPan(
        x: Float, y: Float, viewportWidthPx: Float, viewportHeightPx: Float, scale: Float
    ): Pan {
        val safeScale = normalize(scale)
        if (safeScale == MIN_ZOOM ||
            !viewportWidthPx.isFinite() || !viewportHeightPx.isFinite() ||
            viewportWidthPx <= 0f || viewportHeightPx <= 0f
        ) return Pan(0f, 0f)

        val sourceWidth = QuranPageCachePolicy.PAGE_WIDTH.toFloat()
        val sourceHeight = QuranPageCachePolicy.PAGE_HEIGHT.toFloat()
        val fit = minOf(viewportWidthPx / sourceWidth, viewportHeightPx / sourceHeight)
        val displayedWidth = sourceWidth * fit * safeScale
        val displayedHeight = sourceHeight * fit * safeScale

        // If even the zoomed page is smaller than the available viewport along
        // an axis, its centered image must not slide away from that center.
        val maxX = ((displayedWidth - viewportWidthPx) / 2f).coerceAtLeast(0f)
        val maxY = ((displayedHeight - viewportHeightPx) / 2f).coerceAtLeast(0f)
        return Pan(
            if (x.isFinite()) x.coerceIn(-maxX, maxX) else 0f,
            if (y.isFinite()) y.coerceIn(-maxY, maxY) else 0f
        )
    }
}
