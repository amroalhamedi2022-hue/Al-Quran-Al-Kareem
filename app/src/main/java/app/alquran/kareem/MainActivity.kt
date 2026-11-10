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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, number) {
        value = withContext(Dispatchers.IO) {
            context.assets.open("quran/%03d.png".format(java.util.Locale.ROOT, number)).use {
                BitmapFactory.decodeStream(it)
            }
        }
    }
    Box(modifier.padding(3.dp), contentAlignment = Alignment.Center) {
        if (bitmap == null) CircularProgressIndicator()
        else Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "صفحة $number من المصحف",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
