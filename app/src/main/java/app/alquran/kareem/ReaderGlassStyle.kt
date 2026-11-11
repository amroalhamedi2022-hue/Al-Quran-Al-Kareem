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
internal fun Modifier.readerGlassControls(mode: ReaderThemeMode): Modifier {
    val shape = RoundedCornerShape(22.dp)
    val tint = when (mode) {
        ReaderThemeMode.PEARL -> listOf(
            Color(0xF5FFFFFF), Color(0xE7F6F9FC), Color(0xDBE8F0F5)
        )
        ReaderThemeMode.CHAMPAGNE -> listOf(
            Color(0xF8FFF9EB), Color(0xEAF7EBD4), Color(0xDFE8D6B8)
        )
        ReaderThemeMode.ROYAL_DARK -> listOf(
            Color(0xF438332C), Color(0xEE292622), Color(0xEA1B1B1A)
        )
    }
    val sheen = when (mode) {
        ReaderThemeMode.PEARL -> listOf(
            Color(0xF5FFFFFF), Color(0xBFFFFFFF), Color(0x80869CAC)
        )
        ReaderThemeMode.CHAMPAGNE -> listOf(
            Color(0xFFFFFEF8), Color(0xD8F8E5BC), Color(0x928D6B39)
        )
        ReaderThemeMode.ROYAL_DARK -> listOf(
            Color(0xC8E6C38A), Color(0x758E703D), Color(0xA363512F)
        )
    }
    return this
        .shadow(elevation = 10.dp, shape = shape)
        .clip(shape)
        .background(
            Brush.verticalGradient(tint)
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(sheen),
            shape = shape
        )
}
