package app.alquran.kareem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Basic offline reading actions. Outside the immutable Quran artwork.
 * Premium controls/animations will replace this only after source approval.
 */
@Composable
internal fun ReaderControls(
    page: Int,
    pageCount: Int,
    twoPageSpread: Boolean,
    navigationTwoPageSpread: Boolean,
    bookmarks: Set<Int>,
    readPages: Set<Int>,
    dailyWirdTarget: Int,
    onDailyWirdTargetChanged: (Int) -> Unit,
    surahs: List<SurahIndexEntry>,
    sections: QuranSectionIndex,
    pageAnchors: List<PageVerseAnchor>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJump: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleRead: () -> Unit,
    onResetRead: () -> Unit,
    revealActive: Boolean,
    revealStep: Int,
    onToggleReveal: () -> Unit,
    onRevealMore: () -> Unit,
    onRevealLess: () -> Unit,
    onRevealAll: () -> Unit,
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit,
    keepScreenAwake: Boolean,
    reduceMotion: Boolean,
    appearance: ReaderThemeMode,
    onAppearanceChanged: (ReaderThemeMode) -> Unit,
    onKeepScreenAwakeChanged: (Boolean) -> Unit,
    onReduceMotionChanged: (Boolean) -> Unit,
    onHideControls: () -> Unit
) {
    var toolsVisible by rememberSaveable { mutableStateOf(false) }
    var jumpVisible by rememberSaveable { mutableStateOf(false) }
    var indexVisible by rememberSaveable { mutableStateOf(false) }
    var verseVisible by rememberSaveable { mutableStateOf(false) }
    var verseReference by rememberSaveable { mutableStateOf("") }
    var sectionsVisible by rememberSaveable { mutableStateOf(false) }
    var sectionMode by rememberSaveable { mutableIntStateOf(0) }
    var indexQuery by rememberSaveable { mutableStateOf("") }
    var bookmarksVisible by rememberSaveable { mutableStateOf(false) }
    var resetReadVisible by rememberSaveable { mutableStateOf(false) }
    var pageField by rememberSaveable { mutableStateOf("") }
    val validJump = ReaderNavigationRules.parsePage(pageField, pageCount)
    // The reader must remain legible on narrow phones. No sprawling 7-button row.
    val shownPages = ReaderPagination.visible(page, pageCount, twoPageSpread)
    val shownRead = shownPages.all { it in readPages }
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .readerGlassControls(appearance)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // In Arabic RTL layout, the FIRST child is at the right edge.
            // Keep NEXT on the right to match swiping right to advance.
            TextButton(
                onClick = onNext,
                enabled = ReaderPagination.hasNext(page, pageCount, navigationTwoPageSpread)
            ) { Text("التالي") }
            Text(
                text = if (shownPages.size == 2) {
                    "${shownPages.last()}–${shownPages.first()} / $pageCount"
                } else "${shownPages.first()} / $pageCount",
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onPrevious,
                enabled = ReaderPagination.hasPrevious(page, pageCount, navigationTwoPageSpread)
            ) { Text("السابق") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { toolsVisible = true }) { Text("الأدوات") }
            TextButton(onClick = onToggleBookmark) {
                Text(if (page in bookmarks) "★ محفوظة" else "☆ علامة")
            }
            TextButton(onClick = onHideControls) { Text("إخفاء") }
        }
        LinearProgressIndicator(
            progress = { KhatmaProgressRules.fraction(readPages, pageCount) },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (toolsVisible) {
        AlertDialog(
            onDismissRequest = { toolsVisible = false },
            title = { Text("أدوات القرآن الكريم") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("التنقل والفهارس")
                    TextButton(onClick = {
                        toolsVisible = false
                        pageField = page.toString()
                        jumpVisible = true
                    }) { Text("الانتقال إلى صفحة") }
                    TextButton(
                        onClick = { toolsVisible = false; indexQuery = ""; indexVisible = true },
                        enabled = surahs.size == 114
                    ) { Text("فهرس السور (114)") }
                    TextButton(
                        onClick = { toolsVisible = false; sectionMode = 0; sectionsVisible = true },
                        enabled = sections.juz.size == 30 && sections.hizb.size == 60 && sections.quarters.size == 240
                    ) { Text("الأجزاء والأحزاب") }
                    TextButton(
                        onClick = { toolsVisible = false; verseReference = ""; verseVisible = true },
                        enabled = surahs.size == 114 && pageAnchors.size == 604
                    ) { Text("الانتقال إلى آية") }
                    TextButton(onClick = {
                        toolsVisible = false
                        bookmarksVisible = true
                    }) { Text("العلامات المرجعية (${bookmarks.size})") }
                    Text("اضغط مطولًا على أي صفحة لحفظ علامة لها أو إزالتها؛ حتى في العرض المزدوج.")

                    Text("الختمة — ${readPages.size} من $pageCount صفحة")
                    TextButton(onClick = onToggleRead) {
                        Text(if (shownRead) "إلغاء تعليم الصفحات المقروءة"
                            else "قرأت الصفحات المعروضة")
                    }
                    TextButton(
                        onClick = { toolsVisible = false; resetReadVisible = true },
                        enabled = readPages.isNotEmpty()
                    ) { Text("بدء ختمة جديدة") }

                    Text("خطة الورد اليومي — تحديد يدوي")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onDailyWirdTargetChanged(dailyWirdTarget - 1) },
                            enabled = dailyWirdTarget > DailyWirdRules.MIN_PAGES
                        ) { Text("−") }
                        Text("$dailyWirdTarget صفحة يوميًا")
                        TextButton(
                            onClick = { onDailyWirdTargetChanged(dailyWirdTarget + 1) },
                            enabled = dailyWirdTarget < DailyWirdRules.MAX_PAGES
                        ) { Text("+") }
                    }
                    val nextWird = DailyWirdRules.suggestedNextPages(
                        readPages, pageCount, dailyWirdTarget
                    )
                    if (nextWird.isEmpty()) {
                        Text("أتممت تعليم جميع صفحات الختمة. بارك الله فيك.")
                    } else {
                        Text(
                            "الورد المقترح: " + nextWird.joinToString("، ") +
                            " — المتبقي تقديريًا " +
                            DailyWirdRules.estimatedDaysRemaining(
                                readPages, pageCount, dailyWirdTarget
                            ) + " يومًا"
                        )
                        TextButton(onClick = {
                            onJump(nextWird.first())
                            toolsVisible = false
                        }) { Text("افتح أول صفحة في الورد") }
                    }
                    Text("لا تُعلَّم الصفحات مقروءة إلا بضغطك على زر القراءة.")

                    Text("مظهر أدوات القراءة")
                    ReaderThemeMode.entries.forEach { option ->
                        TextButton(
                            onClick = { onAppearanceChanged(option) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (if (appearance == option) "✓ " else "○ ") +
                                    option.title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    Text("تغيير لون الأدوات والخلفية فقط؛ تبقى صورة المصحف كما هي.")

                    Text("إعدادات القراءة")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("إبقاء الشاشة مضاءة")
                        Switch(
                            checked = keepScreenAwake,
                            onCheckedChange = onKeepScreenAwakeChanged
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("تقليل حركة التقليب")
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = onReduceMotionChanged
                        )
                    }

                    Text("تكبير صفحة المصحف")
                    Text("التكبير الحالي: ${(zoomScale * 100).toInt()}٪")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = onZoomOut,
                            enabled = zoomScale > ReaderZoomRules.MIN_ZOOM
                        ) { Text("−") }
                        TextButton(
                            onClick = onZoomReset,
                            enabled = zoomScale > ReaderZoomRules.MIN_ZOOM
                        ) { Text("١٠٠٪") }
                        TextButton(
                            onClick = onZoomIn,
                            enabled = zoomScale < ReaderZoomRules.MAX_ZOOM
                        ) { Text("+") }
                    }
                    Text(
                        "اضغط مرتين على الصفحة للتكبير أو الرجوع. " +
                        "عند التكبير اسحب لتحريك الصورة؛ يوقف تقليب الصفحات مؤقتًا."
                    )

                    Text("التسميع الذاتي البصري")
                    TextButton(onClick = onToggleReveal) {
                        Text(if (revealActive) "إنهاء التسميع" else "ابدأ التسميع")
                    }
                    if (revealActive) {
                        Text("الكشف التدريجي $revealStep من 15 — ليس بحسب حدود الآيات")
                        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(
                                onClick = onRevealLess,
                                enabled = revealStep > 0
                            ) { Text("إخفاء") }
                            TextButton(
                                onClick = onRevealMore,
                                enabled = revealStep < VisualRevealRules.REVEAL_STEPS
                            ) { Text("كشف") }
                            TextButton(
                                onClick = onRevealAll,
                                enabled = revealStep < VisualRevealRules.REVEAL_STEPS
                            ) { Text("الكل") }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { toolsVisible = false }) { Text("إغلاق") }
            }
        )
    }
    if (resetReadVisible) {
        AlertDialog(
            onDismissRequest = { resetReadVisible = false },
            title = { Text("بدء ختمة جديدة") },
            text = { Text("هل تريد مسح علامات الصفحات التي قرأتها؟ لن تتغير العلامات المرجعية أو موضع القراءة.") },
            confirmButton = {
                TextButton(onClick = {
                    onResetRead()
                    resetReadVisible = false
                }) { Text("ابدأ ختمة جديدة") }
            },
            dismissButton = {
                TextButton(onClick = { resetReadVisible = false }) { Text("إلغاء") }
            }
        )
    }

    if (jumpVisible) {
        AlertDialog(
            onDismissRequest = { jumpVisible = false },
            title = { Text("انتقل إلى صفحة") },
            text = {
                Column {
                    Text("اختر رقمًا من 1 إلى $pageCount")
                    OutlinedTextField(
                        value = pageField,
                        onValueChange = { pageField = it.take(10) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = pageField.isNotBlank() && validJump == null,
                        singleLine = true,
                        label = { Text("رقم الصفحة") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    validJump?.let {
                        onJump(it)
                        jumpVisible = false
                    }
                }, enabled = validJump != null) {
                    Text("انتقال")
                }
            },
            dismissButton = {
                TextButton(onClick = { jumpVisible = false }) { Text("إلغاء") }
            }
        )
    }



    if (verseVisible) {
        val reference = VersePageRules.parseReference(verseReference, surahs)
        val matchingPage = reference?.let {
            VersePageRules.findPage(pageAnchors, it.first, it.second)
        }
        AlertDialog(
            onDismissRequest = { verseVisible = false },
            title = { Text("انتقل إلى آية") },
            text = {
                Column {
                    Text("أدخل رقم السورة ثم الآية، مثل 2:255 أو ٢:٢٥٥")
                    OutlinedTextField(
                        value = verseReference,
                        onValueChange = { verseReference = it.take(16) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("السورة:الآية") },
                        isError = verseReference.isNotBlank() && matchingPage == null
                    )
                    if (matchingPage != null) {
                        Text("مكان الآية في المصحف: صفحة $matchingPage")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        matchingPage?.let(onJump)
                        verseVisible = false
                    },
                    enabled = matchingPage != null
                ) { Text("انتقال") }
            },
            dismissButton = {
                TextButton(onClick = { verseVisible = false }) { Text("إلغاء") }
            }
        )
    }

    if (indexVisible) {
        AlertDialog(
            onDismissRequest = { indexVisible = false },
            title = { Text("فهرس سور القرآن الكريم") },
            text = {
                Column {
                    OutlinedTextField(
                        value = indexQuery,
                        onValueChange = { indexQuery = it.take(60) },
                        label = { Text("اسم السورة أو رقمها") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    val matches = SurahIndexRules.search(surahs, indexQuery)
                    if (matches.isEmpty()) {
                        Text("لم أجد سورة بهذا الاسم.")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 410.dp)) {
                            items(matches, key = { it.number }) { surah ->
                                TextButton(
                                    onClick = {
                                        onJump(surah.startPage)
                                        indexVisible = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${surah.number}. ${surah.name} — صفحة ${surah.startPage}",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { indexVisible = false }) { Text("إغلاق") }
            }
        )
    }


    if (sectionsVisible) {
        val sectionTitle = when (sectionMode) {
            1 -> "الأحزاب"
            2 -> "أرباع الأحزاب"
            else -> "الأجزاء"
        }
        AlertDialog(
            onDismissRequest = { sectionsVisible = false },
            title = { Text("فهرس $sectionTitle") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { sectionMode = 0 }) { Text("30 جزءًا") }
                        TextButton(onClick = { sectionMode = 1 }) { Text("60 حزبًا") }
                        TextButton(onClick = { sectionMode = 2 }) { Text("240 ربعًا") }
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 410.dp)) {
                        val entries = when (sectionMode) {
                            1 -> sections.hizb
                            2 -> sections.quarters
                            else -> sections.juz
                        }
                        items(entries, key = { it.number }) { entry ->
                            TextButton(
                                onClick = {
                                    onJump(entry.page)
                                    sectionsVisible = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (when (sectionMode) {
                                        1 -> "الحزب"
                                        2 -> "الربع"
                                        else -> "الجزء"
                                    }) + " ${entry.number} — صفحة ${entry.page}",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sectionsVisible = false }) { Text("إغلاق") }
            }
        )
    }

    if (bookmarksVisible) {
        AlertDialog(
            onDismissRequest = { bookmarksVisible = false },
            title = { Text("العلامات المرجعية") },
            text = {
                if (bookmarks.isEmpty()) {
                    Text("لم تحفظ أي صفحة بعد.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                        items(bookmarks.sorted()) { bookmarked ->
                            TextButton(onClick = {
                                onJump(bookmarked)
                                bookmarksVisible = false
                            }) {
                                Text("صفحة $bookmarked", modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { bookmarksVisible = false }) { Text("إغلاق") }
            }
        )
    }
}
