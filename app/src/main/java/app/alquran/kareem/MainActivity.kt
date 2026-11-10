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
    val configuration = LocalConfiguration.current
    // Two facing pages are legible on tablets, not on narrow landscape phones.
    val twoPageSpread = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
        configuration.screenWidthDp >= 700
    val positionStore = remember(context) { ReaderPositionStore(context) }
    val bookmarkStore = remember(context) { ReaderBookmarkStore(context) }
    var page by rememberSaveable { mutableIntStateOf(positionStore.read(pageCount)) }
    var bookmarks by remember { mutableStateOf(bookmarkStore.read(pageCount)) }
    LaunchedEffect(page, pageCount) {
        positionStore.save(page, pageCount)
    }
    val visible = ReaderPagination.visible(page, pageCount, twoPageSpread)
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
        ReaderControls(
            page = page,
            pageCount = pageCount,
            twoPageSpread = twoPageSpread,
            bookmarks = bookmarks,
            onPrevious = { page = ReaderPagination.previous(page, pageCount, twoPageSpread) },
            onNext = { page = ReaderPagination.next(page, pageCount, twoPageSpread) },
            onJump = { requested -> page = ReaderPagination.clamp(requested, pageCount) },
            onToggleBookmark = {
                val updated = ReaderNavigationRules.toggleBookmark(bookmarks, page, pageCount)
                bookmarkStore.save(updated, pageCount)
                bookmarks = updated
            }
        )
    }
}

private sealed interface PageImageState {
    data object Loading : PageImageState
    data class Ready(val bitmap: Bitmap) : PageImageState
    data object Failed : PageImageState
}

@Composable
private fun ApprovedPage(number: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var retry by remember(number) { mutableIntStateOf(0) }
    val imageState by produceState<PageImageState>(
        initialValue = PageImageState.Loading,
        key1 = number,
        key2 = retry
    ) {
        value = withContext(Dispatchers.IO) {
            try {
                val file = "quran/%03d.png".format(java.util.Locale.ROOT, number)
                val options = BitmapFactory.Options().apply {
                    inScaled = false
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = context.assets.open(file).use {
                    BitmapFactory.decodeStream(it, null, options)
                }
                // Never substitute a placeholder verse or show truncated ink.
                if (decoded == null) {
                    PageImageState.Failed
                } else if (decoded.width != 1600 || decoded.height != 2287) {
                    decoded.recycle()
                    PageImageState.Failed
                } else {
                    PageImageState.Ready(decoded)
                }
            } catch (_: Exception) {
                // Reading may fail on damaged/undecodable pages; fail closed, never fabricate text.
                PageImageState.Failed
            }
        }
    }
    // Original official artwork has transparent pixels: a solid paper backing is required.
    Box(modifier.padding(3.dp).background(Color.White), contentAlignment = Alignment.Center) {
        when (val image = imageState) {
            is PageImageState.Loading -> CircularProgressIndicator()
            is PageImageState.Ready -> Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = "صفحة $number من المصحف",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            is PageImageState.Failed -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "تعذر عرض صفحة المصحف رقم $number. لن يُعرض نص بديل.",
                    textAlign = TextAlign.Center
                )
                Button(onClick = { retry += 1 }) { Text("إعادة المحاولة") }
            }
        }
    }
}
