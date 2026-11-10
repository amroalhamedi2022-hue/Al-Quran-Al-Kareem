package app.alquran.kareem

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    QuranApp()
                }
            }
        }
    }
}

@Composable
private fun QuranApp() {
    val assets = LocalContext.current.assets
    var status by remember { mutableStateOf<QuranAssetVerifier.Status?>(null) }
    LaunchedEffect(assets) {
        status = QuranAssetVerifier.check(assets)
    }
    when (val verified = status) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is QuranAssetVerifier.Status.Unavailable -> Box(
            Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "القرآن الكريم\n\n${verified.reason}\n\nلن يتم عرض أي صفحة قبل اعتماد المصحف كاملًا.",
                textAlign = TextAlign.Center
            )
        }
        is QuranAssetVerifier.Status.Approved -> Reader(verified.pages)
    }
}

@Composable
private fun Reader(pageCount: Int) {
    val context = LocalContext.current
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val positionStore = remember(context) { ReaderPositionStore(context) }
    var page by rememberSaveable { mutableIntStateOf(positionStore.read(pageCount)) }
    LaunchedEffect(page, pageCount) {
        positionStore.save(page, pageCount)
    }
    val right = ReaderPagination.rightPage(page, pageCount)
    val visible = ReaderPagination.visible(page, pageCount, landscape)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                visible.forEach { number ->
                    ApprovedPage(number, Modifier.weight(1f).fillMaxSize())
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { page = ReaderPagination.previous(page, pageCount, landscape) },
                enabled = ReaderPagination.hasPrevious(page, pageCount, landscape)
            ) {
                Text("السابق")
            }
            Text(if (landscape) "${right}–${(right + 1).coerceAtMost(pageCount)} / $pageCount" else "$page / $pageCount")
            Button(
                onClick = { page = ReaderPagination.next(page, pageCount, landscape) },
                enabled = ReaderPagination.hasNext(page, pageCount, landscape)
            ) {
                Text("التالي")
            }
        }
    }
}

@Composable
private fun ApprovedPage(number: Int, modifier: Modifier = Modifier) {
    val assets = LocalContext.current.assets
    // The composition slot is recreated on every page change: never briefly
    // display the previous Quran page with the new page number.
    key(number) {
        val state by produceState<Pair<Bitmap?, Boolean>>(
            initialValue = Pair(null, false), number, assets
        ) {
            value = withContext(Dispatchers.IO) {
                try {
                    val decoded = assets.open(
                        "quran/%03d.png".format(java.util.Locale.ROOT, number)
                    ).use { stream -> BitmapFactory.decodeStream(stream) }
                    if (decoded == null) Pair(null, true) else Pair(decoded, false)
                } catch (_: Exception) {
                    // An invalid page must never be silently replaced by another.
                    Pair(null, true)
                }
            }
        }
        Box(
            modifier = modifier.padding(3.dp).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.second -> Text(
                    text = "تعذّر عرض الصفحة $number من المصحف.",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                state.first == null -> CircularProgressIndicator()
                else -> state.first?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "صفحة $number من المصحف",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
