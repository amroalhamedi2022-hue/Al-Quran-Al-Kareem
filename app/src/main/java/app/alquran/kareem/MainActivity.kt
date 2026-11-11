package app.alquran.kareem

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

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
        val approved = QuranAssetVerifier.check(assets)
        // Exact original publisher images are accepted in signed release too.
        // Source integrity is not a claim of institutional certification.
        status = if (approved is QuranAssetVerifier.Status.Unavailable) {
            QuranPublisherSourceVerifier.check(assets) ?: approved
        } else approved
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
        is QuranAssetVerifier.Status.SourceVerified -> Reader(verified.pages)
    }
}

@Composable
private fun Reader(pageCount: Int) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    // Two facing pages are legible on tablets, not on narrow landscape phones.
    val twoPageSpread = ReaderViewportRules.twoPageSpread(
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        configuration.screenWidthDp
    )
    val positionStore = remember(context) { ReaderPositionStore(context) }
    val bookmarkStore = remember(context) { ReaderBookmarkStore(context) }
    val khatmaStore = remember(context) { KhatmaProgressStore(context) }
    val pageRepository = remember(context) { QuranPageBitmapRepository(context) }
    // The complete source-checked 114-Surah index stays bundled for offline use.
    val surahs = remember(context) { SurahIndexStore.load(context.assets) }
    val sections = remember(context) { QuranSectionStore.load(context.assets) }
    val pageAnchors = remember(context) { VersePageStore.load(context.assets) }
    DisposableEffect(pageRepository) {
        onDispose { pageRepository.trim() }
    }
    var page by rememberSaveable { mutableIntStateOf(positionStore.read(pageCount)) }
    var bookmarks by remember { mutableStateOf(bookmarkStore.read(pageCount)) }
    var readPages by remember { mutableStateOf(khatmaStore.read(pageCount)) }
    var showControls by rememberSaveable { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    LaunchedEffect(page, pageCount) {
        positionStore.save(page, pageCount)
    }
    val visible = ReaderPagination.visible(page, pageCount, twoPageSpread)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.weight(1f).fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyUp) {
                            false
                        } else {
                            when (event.key) {
                                Key.DirectionRight, Key.PageDown -> {
                                    if (ReaderPagination.hasNext(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.next(page, pageCount, twoPageSpread)
                                    }
                                    true
                                }
                                Key.DirectionLeft, Key.PageUp -> {
                                    if (ReaderPagination.hasPrevious(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.previous(page, pageCount, twoPageSpread)
                                    }
                                    true
                                }
                                Key.Escape -> {
                                    showControls = true
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                    .focusable()
                    .pointerInput(page, twoPageSpread) {
                        detectTapGestures(onTap = { offset ->
                            when (ReaderTouchRules.classify(offset.x, size.width.toFloat())) {
                                ReaderTouchRules.Action.NEXT -> {
                                    if (ReaderPagination.hasNext(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.next(page, pageCount, twoPageSpread)
                                    }
                                }
                                ReaderTouchRules.Action.PREVIOUS -> {
                                    if (ReaderPagination.hasPrevious(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.previous(page, pageCount, twoPageSpread)
                                    }
                                }
                                ReaderTouchRules.Action.TOGGLE_CONTROLS -> showControls = !showControls
                                ReaderTouchRules.Action.NONE -> Unit
                            }
                        })
                    }
                    .pointerInput(page, twoPageSpread) {
                    var swipeDistance = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { swipeDistance = 0f },
                        onHorizontalDrag = { change, amount ->
                            swipeDistance += amount
                            change.consume()
                        },
                        onDragEnd = {
                            when (ReaderGestureRules.classify(swipeDistance, 64.dp.toPx())) {
                                ReaderGestureRules.Turn.NEXT ->
                                    if (ReaderPagination.hasNext(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.next(page, pageCount, twoPageSpread)
                                    }
                                ReaderGestureRules.Turn.PREVIOUS ->
                                    if (ReaderPagination.hasPrevious(page, pageCount, twoPageSpread)) {
                                        page = ReaderPagination.previous(page, pageCount, twoPageSpread)
                                    }
                                ReaderGestureRules.Turn.NONE -> Unit
                            }
                            swipeDistance = 0f
                        },
                        onDragCancel = { swipeDistance = 0f }
                    )
                },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                visible.forEach { number ->
                    // Each navigation target has a separate Compose identity. Never
                    // leave the previous page bitmap on screen while new art loads.
                    key(number) {
                        ApprovedPage(
                            number,
                            pageRepository,
                            Modifier.weight(1f).fillMaxSize()
                        )
                    }
                }
            }
        }
        AnimatedVisibility(visible = showControls) {
            ReaderControls(
                page = page,
            pageCount = pageCount,
            twoPageSpread = twoPageSpread,
            bookmarks = bookmarks,
            readPages = readPages,
            surahs = surahs,
            sections = sections,
            pageAnchors = pageAnchors,
            onPrevious = { page = ReaderPagination.previous(page, pageCount, twoPageSpread) },
            onNext = { page = ReaderPagination.next(page, pageCount, twoPageSpread) },
            onJump = { requested -> page = ReaderPagination.clamp(requested, pageCount) },
                onToggleBookmark = {
                    val updated = ReaderNavigationRules.toggleBookmark(bookmarks, page, pageCount)
                    bookmarkStore.save(updated, pageCount)
                    bookmarks = updated
                },
                onToggleRead = {
                val updated = KhatmaProgressRules.toggleVisible(readPages, visible, pageCount)
                khatmaStore.save(updated, pageCount)
                readPages = updated
            },
            onResetRead = {
                khatmaStore.clear()
                readPages = emptySet()
            },
            onHideControls = { showControls = false }
            )
        }
    }
}

private sealed interface PageImageState {
    data object Loading : PageImageState
    data class Ready(val bitmap: Bitmap) : PageImageState
    data object Failed : PageImageState
}

@Composable
private fun ApprovedPage(
    number: Int,
    repository: QuranPageBitmapRepository,
    modifier: Modifier = Modifier
) {
    var retry by remember(number) { mutableIntStateOf(0) }
    val imageState by produceState<PageImageState>(
        initialValue = PageImageState.Loading,
        key1 = number,
        key2 = retry,
        key3 = repository
    ) {
        // Immutable original PNG bitmap only. On failure show an error state,
        // not a stale page nor fabricated replacement text.
        value = repository.load(number)?.let { PageImageState.Ready(it) }
            ?: PageImageState.Failed
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
