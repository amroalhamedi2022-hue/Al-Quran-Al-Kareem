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

    fun clampPan(
        x: Float, y: Float, viewportWidthPx: Float, viewportHeightPx: Float, scale: Float
    ): Pan {
        val safeScale = normalize(scale)
        if (safeScale == MIN_ZOOM || !viewportWidthPx.isFinite() ||
            !viewportHeightPx.isFinite() || viewportWidthPx <= 0f || viewportHeightPx <= 0f
        ) return Pan(0f, 0f)
        val maxX = viewportWidthPx * (safeScale - 1f) / 2f
        val maxY = viewportHeightPx * (safeScale - 1f) / 2f
        return Pan(
            if (x.isFinite()) x.coerceIn(-maxX, maxX) else 0f,
            if (y.isFinite()) y.coerceIn(-maxY, maxY) else 0f
        )
    }
}
