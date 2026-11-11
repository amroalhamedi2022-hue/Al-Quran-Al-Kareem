package app.alquran.kareem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lightweight frosted-glass styling used ONLY below the Quran image viewport.
 * We deliberately avoid expensive page-wide blurs or overlays that could
 * obscure/alter any Quran glyph. No false claim of physical refraction.
 */
internal fun Modifier.readerGlassControls(): Modifier {
    val shape = RoundedCornerShape(22.dp)
    return this
        .shadow(elevation = 10.dp, shape = shape)
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    Color(0xEEFFFFFF),
                    Color(0xDCF7F9FC),
                    Color(0xC9E9EDF2)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color(0xF5FFFFFF), Color(0xA7FFFFFF), Color(0x6697A9B6))
            ),
            shape = shape
        )
}
