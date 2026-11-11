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
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJump: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onHideControls: () -> Unit
) {
    var jumpVisible by rememberSaveable { mutableStateOf(false) }
    var bookmarksVisible by rememberSaveable { mutableStateOf(false) }
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
