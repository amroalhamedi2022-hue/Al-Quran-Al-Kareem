package app.alquran.kareem

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
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
                text = "القرآن الكريم\n\n${verified.reason}\n\nلن يتم عرض صفحات ناقصة أو مختلفة عن المصدر الأصلي المتحقق منه.",
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
    // Zoom uses one page at a time to prevent magnified pages overlapping
    // the neighbor on tablets. The original image is intact at 100%.
    var zoomScale by rememberSaveable { mutableFloatStateOf(ReaderZoomRules.MIN_ZOOM) }
    var zoomPanX by remember { mutableFloatStateOf(0f) }
    var zoomPanY by remember { mutableFloatStateOf(0f) }
    var pageViewportHeightPx by remember { mutableFloatStateOf(1f) }
    val twoPageSpread = ReaderViewportRules.twoPageSpread(
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        configuration.screenWidthDp
    ) && !ReaderZoomRules.isZoomed(zoomScale)
    val positionStore = remember(context) { ReaderPositionStore(context) }
    val bookmarkStore = remember(context) { ReaderBookmarkStore(context) }
    val khatmaStore = remember(context) { KhatmaProgressStore(context) }
    val dailyWirdStore = remember(context) { DailyWirdStore(context) }
    val displaySettingsStore = remember(context) { ReaderDisplaySettingsStore(context) }
    var displaySettings by remember { mutableStateOf(displaySettingsStore.read()) }
    val hostView = LocalView.current
    // The Android view's keepScreenOn flag is scoped to the reader lifecycle.
    // It is off by default, and cleared when reader leaves composition.
    DisposableEffect(hostView, displaySettings.keepScreenAwake) {
        hostView.keepScreenOn = displaySettings.keepScreenAwake
        onDispose { hostView.keepScreenOn = false }
    }
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
    var dailyWirdTarget by remember { mutableIntStateOf(dailyWirdStore.readTarget()) }
    var showControls by rememberSaveable { mutableStateOf(true) }
    var revealActive by rememberSaveable { mutableStateOf(false) }
    var revealStep by rememberSaveable { mutableIntStateOf(0) }
    var turnOffsetPx by remember { mutableFloatStateOf(0f) }
    var pageViewportWidthPx by remember { mutableFloatStateOf(1f) }
    var isTurning by remember { mutableStateOf(false) }
    val turnScope = rememberCoroutineScope()

    fun setZoom(requested: Float) {
        zoomScale = ReaderZoomRules.normalize(requested)
        val safe = ReaderZoomRules.clampPan(
            zoomPanX, zoomPanY,
            pageViewportWidthPx, pageViewportHeightPx, zoomScale
        )
        zoomPanX = safe.x
        zoomPanY = safe.y
    }

    fun requestPageTurn(forward: Boolean) {
        if (isTurning) return
        // The temporary single-page zoom view must not change tablet book
        // pagination. Resolve the NEXT full spread before dropping the zoom.
        val normalTwoPageSpread = ReaderViewportRules.twoPageSpread(
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
            configuration.screenWidthDp
        )
        val target = ReaderTurnTargetRules.target(
            page, pageCount, normalTwoPageSpread, forward
        )
        if (ReaderZoomRules.isZoomed(zoomScale)) setZoom(1f)
        if (!ReaderMotionRules.animateTurn(displaySettings.reduceMotion)) {
            turnOffsetPx = 0f
            if (target != page) page = target
            return
        }
        val initial = turnOffsetPx
        turnScope.launch {
            isTurning = true
            try {
                if (target == page) {
                    // Page boundary: spring the sheet back rather than crossing it.
                    animate(initial, 0f, animationSpec = tween(150)) { value, _ ->
                        turnOffsetPx = value
                    }
                } else {
                    // Load the destination before leaving the readable page.
                    // A failed load still produces the normal fail-closed error page.
                    ReaderPagination.visible(target, pageCount, normalTwoPageSpread)
                        .forEach { pageRepository.load(it) }
                    val exit = (if (forward) 1f else -1f) * pageViewportWidthPx
                    animate(initial, exit, animationSpec = tween(190)) { value, _ ->
                        turnOffsetPx = value
                    }
                    page = target
                    // Enter from the opposite hinge. Never rotate beyond 78°,
                    // which would mirror the Arabic page during the turn.
                    turnOffsetPx = -exit
                    animate(-exit, 0f, animationSpec = tween(190)) { value, _ ->
                        turnOffsetPx = value
                    }
                }
            } finally {
                turnOffsetPx = 0f
                isTurning = false
            }
        }
    }

    fun cancelDragTurn() {
        if (isTurning) return
        if (!ReaderMotionRules.animateTurn(displaySettings.reduceMotion)) {
            turnOffsetPx = 0f
            return
        }
        turnScope.launch {
            isTurning = true
            try {
                animate(turnOffsetPx, 0f, animationSpec = tween(150)) { value, _ ->
                    turnOffsetPx = value
                }
            } finally {
                turnOffsetPx = 0f
                isTurning = false
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    LaunchedEffect(page, pageCount) {
        positionStore.save(page, pageCount)
        setZoom(1f)
    }
    // Warm only a verified neighboring image, and only on devices with enough
    // heap for the currently visible full-size pages plus one more bitmap.
    // Navigation cancels this coroutine, so no stale prefetch takes precedence.
    LaunchedEffect(page, twoPageSpread, pageRepository, pageCount) {
        delay(160)
        val shown = ReaderPagination.visible(page, pageCount, twoPageSpread)
        shown.forEach { pageRepository.load(it) }
        pageRepository.prefetchTarget(page, pageCount, twoPageSpread)
            ?.let { pageRepository.load(it) }
    }
    val visible = ReaderPagination.visible(page, pageCount, twoPageSpread)
    // Never infer verse boundaries: only an opt-in paper mask over immutable pixels.
    LaunchedEffect(page) {
        // Magnification and orientation do not mean the reader changed page.
        // Keep the self-test reveal state unless the actual page changes.
        if (revealActive) revealStep = 0
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.weight(1f).fillMaxWidth()
                    // Keep a magnified image away from navigation controls.
                    .clipToBounds()
                    .onSizeChanged {
                        pageViewportWidthPx = it.width.toFloat().coerceAtLeast(1f)
                        pageViewportHeightPx = it.height.toFloat().coerceAtLeast(1f)
                        val safe = ReaderZoomRules.clampPan(
                            zoomPanX, zoomPanY, pageViewportWidthPx, pageViewportHeightPx, zoomScale
                        )
                        zoomPanX = safe.x
                        zoomPanY = safe.y
                    }
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyUp) {
                            false
                        } else {
                            when (event.key) {
                                Key.DirectionRight, Key.PageDown -> {
                                    requestPageTurn(true)
                                    true
                                }
                                Key.DirectionLeft, Key.PageUp -> {
                                    requestPageTurn(false)
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
                    .pointerInput(page, twoPageSpread, zoomScale) {
                        detectTapGestures(
                            onDoubleTap = {
                                setZoom(ReaderZoomRules.toggleDoubleTap(zoomScale))
                            },
                            onTap = { offset ->
                                if (ReaderZoomRules.isZoomed(zoomScale)) {
                                    // No accidental page turns while magnifying the Quran.
                                    showControls = !showControls
                                } else {
                                    when (ReaderTouchRules.classify(offset.x, size.width.toFloat())) {
                                        ReaderTouchRules.Action.NEXT -> requestPageTurn(true)
                                        ReaderTouchRules.Action.PREVIOUS -> requestPageTurn(false)
                                        ReaderTouchRules.Action.TOGGLE_CONTROLS ->
                                            showControls = !showControls
                                        ReaderTouchRules.Action.NONE -> Unit
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(page, twoPageSpread, zoomScale, displaySettings.reduceMotion) {
                        var swipeDistance = 0f
                        if (!ReaderZoomRules.isZoomed(zoomScale)) detectHorizontalDragGestures(
                            onDragStart = {
                                swipeDistance = 0f
                                if (!isTurning) turnOffsetPx = 0f
                            },
                            onHorizontalDrag = { change, amount ->
                                if (!isTurning) {
                                    swipeDistance += amount
                                    if (ReaderMotionRules.animateTurn(displaySettings.reduceMotion)) {
                                        turnOffsetPx = (turnOffsetPx + amount)
                                            .coerceIn(-pageViewportWidthPx, pageViewportWidthPx)
                                    }
                                }
                                change.consume()
                            },
                            onDragEnd = {
                                if (!isTurning) {
                                    when (ReaderGestureRules.classify(swipeDistance, 64.dp.toPx())) {
                                        ReaderGestureRules.Turn.NEXT -> requestPageTurn(true)
                                        ReaderGestureRules.Turn.PREVIOUS -> requestPageTurn(false)
                                        ReaderGestureRules.Turn.NONE -> cancelDragTurn()
                                    }
                                }
                                swipeDistance = 0f
                            },
                            onDragCancel = {
                                swipeDistance = 0f
                                cancelDragTurn()
                            }
                        )
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                visible.forEachIndexed { index, number ->
                    // Only the sheet being turned is transformed. Still pages
                    // retain the publisher's original untransformed Quran artwork.
                    key(number) {
                        ApprovedPage(
                            number,
                            pageRepository,
                            revealFraction = if (revealActive) VisualRevealRules.fraction(revealStep) else 1f,
                            modifier = Modifier.weight(1f).fillMaxSize()
                                .pointerInput(number, zoomScale) {
                                    if (ReaderZoomRules.isZoomed(zoomScale)) {
                                        detectDragGestures { change, offset ->
                                            change.consume()
                                            val moved = ReaderZoomRules.clampPan(
                                                zoomPanX + offset.x,
                                                zoomPanY + offset.y,
                                                pageViewportWidthPx,
                                                pageViewportHeightPx,
                                                zoomScale
                                            )
                                            zoomPanX = moved.x
                                            zoomPanY = moved.y
                                        }
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = zoomPanX
                                    translationY = zoomPanY
                                }
                                .graphicsLayer {
                                val pose = ReaderPaperTurnRules.pose(turnOffsetPx, pageViewportWidthPx)
                                if (ReaderMotionRules.animateTurn(displaySettings.reduceMotion) &&
                                    ReaderPaperTurnRules.shouldTiltPage(
                                        index, twoPageSpread, turnOffsetPx
                                    )
                                ) {
                                    rotationY = pose.rotationDegreesY
                                    transformOrigin = TransformOrigin(pose.pivotX, 0.5f)
                                    cameraDistance = size.width * 6f + 1f
                                    shadowElevation = 10.dp.toPx() * pose.elevationFraction
                                }
                            }
                        )
                    }
                }
            }
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AnimatedVisibility(visible = showControls) {
                ReaderControls(
                page = page,
            pageCount = pageCount,
            twoPageSpread = twoPageSpread,
            bookmarks = bookmarks,
            readPages = readPages,
            dailyWirdTarget = dailyWirdTarget,
            onDailyWirdTargetChanged = { target ->
                val next = DailyWirdRules.normalizeTarget(target)
                dailyWirdStore.saveTarget(next)
                dailyWirdTarget = next
            },
            surahs = surahs,
            sections = sections,
            pageAnchors = pageAnchors,
            onPrevious = { requestPageTurn(false) },
            onNext = { requestPageTurn(true) },
            onJump = { requested ->
                if (!isTurning) {
                    turnOffsetPx = 0f
                    page = ReaderPagination.clamp(requested, pageCount)
                }
            },
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
            revealActive = revealActive,
            revealStep = revealStep,
            onToggleReveal = {
                revealActive = !revealActive
                revealStep = 0
                showControls = true
            },
            onRevealMore = { revealStep = VisualRevealRules.next(revealStep) },
            onRevealLess = { revealStep = VisualRevealRules.previous(revealStep) },
            onRevealAll = { revealStep = VisualRevealRules.REVEAL_STEPS },
            zoomScale = zoomScale,
            onZoomIn = { setZoom(ReaderZoomRules.zoomIn(zoomScale)) },
            onZoomOut = { setZoom(ReaderZoomRules.zoomOut(zoomScale)) },
            onZoomReset = { setZoom(ReaderZoomRules.MIN_ZOOM) },
            keepScreenAwake = displaySettings.keepScreenAwake,
            reduceMotion = displaySettings.reduceMotion,
            onKeepScreenAwakeChanged = { enabled ->
                val updated = displaySettings.copy(keepScreenAwake = enabled)
                displaySettingsStore.save(updated)
                displaySettings = updated
            },
            onReduceMotionChanged = { enabled ->
                val updated = displaySettings.copy(reduceMotion = enabled)
                displaySettingsStore.save(updated)
                displaySettings = updated
                turnOffsetPx = 0f
            },
            onHideControls = { showControls = false }
                )
            }
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
    revealFraction: Float = 1f,
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
            is PageImageState.Ready -> Box(Modifier.fillMaxSize()) {
                Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = "صفحة $number من المصحف",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
                )
                if (revealFraction < 1f) {
                    // Opaque sheet conceals pixels ONLY, and is removed to reveal
                    // the original PNG with no retyping, AI, OCR or resampling.
                    Box(
                        Modifier.fillMaxWidth()
                            .fillMaxHeight((1f - revealFraction).coerceIn(0f,1f))
                            .align(Alignment.BottomCenter)
                            .background(Color.White)
                    )
                }
            }
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
