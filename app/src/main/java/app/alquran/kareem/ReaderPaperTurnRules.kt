package app.alquran.kareem

import kotlin.math.abs

/**
 * Source-preserving 3D sheet-hinge turn (not yet curved mesh).
 * Quran artwork is NEVER re-rendered, mirrored, cropped or changed at rest.
 * Only the GPU presentation layer tilts during a deliberate page turn.
 */
internal object ReaderPaperTurnRules {
    private const val MAX_DEGREES = 78f

    data class Pose(
        val rotationDegreesY: Float,
        val pivotX: Float,
        val elevationFraction: Float
    )

    fun pose(dragPixels: Float, viewportWidthPixels: Float): Pose {
        if (!dragPixels.isFinite() || !viewportWidthPixels.isFinite() ||
            viewportWidthPixels <= 0f || dragPixels == 0f
        ) return Pose(0f, 0.5f, 0f)

        val fraction = (abs(dragPixels) / viewportWidthPixels).coerceIn(0f, 1f)
        return Pose(
            rotationDegreesY = if (dragPixels > 0) -MAX_DEGREES * fraction
                else MAX_DEGREES * fraction,
            pivotX = if (dragPixels > 0) 1f else 0f,
            elevationFraction = fraction
        )
    }

    fun shouldTiltPage(pageInSpread: Int, isTwoPageSpread: Boolean, dragPixels: Float): Boolean =
        !isTwoPageSpread || (dragPixels > 0f && pageInSpread == 0) ||
            (dragPixels < 0f && pageInSpread == 1)
}
