package app.alquran.kareem

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    bookmarks: Set<Int>,
    readPages: Set<Int>,
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
    onHideControls: () -> Unit
) {
    var jumpVisible by rememberSaveable { mutableStateOf(false) }
    var indexVisible by rememberSaveable { mutableStateOf(false) }
    var verseVisible by rememberSaveable { mutableStateOf(false) }
    var verseReference by rememberSaveable { mutableStateOf("") }
    var sectionsVisible by rememberSaveable { mutableStateOf(false) }
    var showHizb by rememberSaveable { mutableStateOf(false) }
    var indexQuery by rememberSaveable { mutableStateOf("") }
    var bookmarksVisible by rememberSaveable { mutableStateOf(false) }
    var resetReadVisible by rememberSaveable { mutableStateOf(false) }
    var pageField by rememberSaveable { mutableStateOf("") }
    val validJump = ReaderNavigationRules.parsePage(pageField, pageCount)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = onPrevious,
                enabled = ReaderPagination.hasPrevious(page, pageCount, twoPageSpread)
            ) { Text("السابق") }
            val visible = ReaderPagination.visible(page, pageCount, twoPageSpread)
            Text(
                text = if (visible.size == 2) {
                    "صفحتا ${visible[1]} و${visible[0]} / $pageCount"
                } else "صفحة ${visible.first()} / $pageCount",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Button(
                onClick = onNext,
                enabled = ReaderPagination.hasNext(page, pageCount, twoPageSpread)
            ) { Text("التالي") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = {
                pageField = page.toString()
                jumpVisible = true
            }) { Text("انتقل لصفحة") }
            TextButton(
                onClick = { indexQuery = ""; indexVisible = true },
                enabled = surahs.size == 114
            ) { Text("فهرس السور") }
            TextButton(
                onClick = { verseReference = ""; verseVisible = true },
                enabled = surahs.size == 114 && pageAnchors.size == 604
            ) { Text("الآية") }
            TextButton(
                onClick = { showHizb = false; sectionsVisible = true },
                enabled = sections.juz.size == 30 && sections.hizb.size == 60
            ) { Text("الأجزاء والأحزاب") }
            TextButton(onClick = onToggleBookmark) {
                Text(if (page in bookmarks) "إزالة العلامة" else "وضع علامة")
            }
            TextButton(onClick = { bookmarksVisible = true }) {
                Text("العلامات (${bookmarks.size})")
            }
            TextButton(onClick = onHideControls) {
                Text("إخفاء الأدوات")
            }
        }
        val shownPages = ReaderPagination.visible(page, pageCount, twoPageSpread)
        val shownRead = shownPages.all { it in readPages }
        Text(
            text = "الختمة اليدوية: ${readPages.size} من $pageCount صفحة",
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
        )
        LinearProgressIndicator(
            progress = { KhatmaProgressRules.fraction(readPages, pageCount) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = onToggleReveal) {
                Text(if (revealActive) "إنهاء التسميع البصري" else "التسميع البصري")
            }
            if (revealActive) {
                TextButton(onClick = onRevealLess, enabled = revealStep > 0) {
                    Text("إخفاء خطوة")
                }
                TextButton(
                    onClick = onRevealMore,
                    enabled = revealStep < VisualRevealRules.REVEAL_STEPS
                ) { Text("كشف خطوة") }
                TextButton(
                    onClick = onRevealAll,
                    enabled = revealStep < VisualRevealRules.REVEAL_STEPS
                ) { Text("كشف الكل") }
            }
        }
        if (revealActive) {
            Text(
                "كشف الصفحة تدريجيًا: $revealStep من 15. هذه خطوات بصرية لا تمثل حدود الآيات."
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onToggleRead) {
                Text(if (shownRead) "إلغاء تعليم المعروض" else "قرأت الصفحات المعروضة")
            }
            TextButton(onClick = { resetReadVisible = true }, enabled = readPages.isNotEmpty()) {
                Text("بدء ختمة جديدة")
            }
        }
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
        AlertDialog(
            onDismissRequest = { sectionsVisible = false },
            title = { Text(if (showHizb) "فهرس الأحزاب" else "فهرس الأجزاء") },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = { showHizb = false }) { Text("30 جزءًا") }
                        TextButton(onClick = { showHizb = true }) { Text("60 حزبًا") }
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 410.dp)) {
                        val entries = if (showHizb) sections.hizb else sections.juz
                        items(entries, key = { it.number }) { entry ->
                            TextButton(
                                onClick = {
                                    onJump(entry.page)
                                    sectionsVisible = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (if (showHizb) "الحزب" else "الجزء") +
                                        " ${entry.number} — صفحة ${entry.page}",
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
