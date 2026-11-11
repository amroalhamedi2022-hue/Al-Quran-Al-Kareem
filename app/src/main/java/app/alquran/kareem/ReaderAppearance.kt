package app.alquran.kareem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Only the surrounding app chrome changes theme; the Quran page stays on the
 * same WHITE sheet with its exact original image pixels in every theme.
 */
internal enum class ReaderThemeMode(val storageId: String, val title: String) {
    PEARL("pearl", "لؤلؤي"),
    CHAMPAGNE("champagne", "شامبانيا"),
    ROYAL_DARK("royal_dark", "ذهبي داكن");

    companion object {
        fun fromStorage(value: String?): ReaderThemeMode =
            entries.firstOrNull { it.storageId == value } ?: PEARL
    }
}

private val PearlColors = lightColorScheme(
    primary = Color(0xFF314F61),
    onPrimary = Color.White,
    background = Color(0xFFF3F5F6),
    onBackground = Color(0xFF172A36),
    surface = Color(0xFFF4F7F8),
    onSurface = Color(0xFF172A36),
    surfaceVariant = Color(0xFFE4EAF0),
    onSurfaceVariant = Color(0xFF334E5F),
    outline = Color(0xFF718D9B)
)

private val ChampagneColors = lightColorScheme(
    primary = Color(0xFF72501E),
    onPrimary = Color.White,
    background = Color(0xFFF4EBDA),
    onBackground = Color(0xFF42331D),
    surface = Color(0xFFFBF3E6),
    onSurface = Color(0xFF40321E),
    surfaceVariant = Color(0xFFEEE0C6),
    onSurfaceVariant = Color(0xFF654A28),
    outline = Color(0xFFAF9368)
)

private val RoyalDarkColors = darkColorScheme(
    primary = Color(0xFFE4BA6D),
    onPrimary = Color(0xFF21170C),
    background = Color(0xFF141312),
    onBackground = Color(0xFFF9EAD3),
    surface = Color(0xFF211F1D),
    onSurface = Color(0xFFF7E9D2),
    surfaceVariant = Color(0xFF37302A),
    onSurfaceVariant = Color(0xFFF1DBBA),
    outline = Color(0xFFBC9658)
)

@Composable
internal fun ReaderAppearanceTheme(mode: ReaderThemeMode, content: @Composable () -> Unit) {
    val colors = when (mode) {
        ReaderThemeMode.PEARL -> PearlColors
        ReaderThemeMode.CHAMPAGNE -> ChampagneColors
        ReaderThemeMode.ROYAL_DARK -> RoyalDarkColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
