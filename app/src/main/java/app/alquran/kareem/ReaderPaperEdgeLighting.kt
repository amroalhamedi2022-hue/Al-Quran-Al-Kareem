package app.alquran.kareem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Purely transient GPU lighting over the turning paper edge.
 * This is NOT a replacement for a future curved 3D paper mesh.
 *
 * Nothing is painted over a stationary Quran page, and shading remains within
 * the ContentScale.Fit rectangle of the untouched source image.
 */
internal fun Modifier.readerPaperEdgeLighting(
    dragPixels: Float,
    viewportWidthPixels: Float,
    activeSheet: Boolean
): Modifier = drawWithContent {
    drawContent()
    if (!activeSheet) return@drawWithContent
    val light = ReaderPaperLightingRules.light(dragPixels, viewportWidthPixels)
    if (light.shadowAlpha <= 0f) return@drawWithContent

    val bounds = ReaderPaperLightingRules.fittedBounds(size.width, size.height)
        ?: return@drawWithContent
    val band = bounds.width * light.bandFraction
    if (band <= 0f) return@drawWithContent
    val rightHinge = dragPixels > 0f
    val start = if (rightHinge) bounds.left + bounds.width - band else bounds.left
    val colors = if (rightHinge) {
        listOf(
            Color.Transparent,
            Color.White.copy(alpha = light.highlightAlpha),
            Color.Black.copy(alpha = light.shadowAlpha)
        )
    } else {
        listOf(
            Color.Black.copy(alpha = light.shadowAlpha),
            Color.White.copy(alpha = light.highlightAlpha),
            Color.Transparent
        )
    }
    drawRect(
        brush = Brush.horizontalGradient(colors, startX = start, endX = start + band),
        topLeft = Offset(start, bounds.top),
        size = Size(band, bounds.height)
    )
}
