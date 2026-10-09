package com.mcode.trail.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mcode.trail.data.local.Bookmark
import com.mcode.trail.data.local.TrailDatabase
import kotlinx.coroutines.flow.first

/**
 * Widget تطبيق أثر — نسخة نظيفة
 *
 * 3 أحجام:
 * - Small  (2x2): العدد + آخر إضافة
 * - Medium (4x2): آخر 3 إضافات
 * - Large  (4x4): آخر 6 إضافات + footer
 */

// ═══════ الألوان ═══════
private val COLOR_BG = Color(0xFFFAF6F1)
private val COLOR_PRIMARY = Color(0xFFD97757)
private val COLOR_TEXT_DARK = Color(0xFF2B1F1A)
private val COLOR_TEXT_MUTED = Color(0xFF7A6A5F)
private val COLOR_DIVIDER = Color(0xFFE8DDD0)

class TrailWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),   // Small
            DpSize(250.dp, 110.dp),   // Medium
            DpSize(250.dp, 250.dp)    // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = TrailDatabase.getDatabase(context)

        val bookmarks = runCatching {
            database.bookmarkDao().getAllBookmarks().first()
        }.getOrDefault(emptyList())

        provideContent {
            GlanceTheme {
                WidgetContent(
                    bookmarks = bookmarks,
                    currentSize = LocalSize.current
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    bookmarks: List<Bookmark>,
    currentSize: DpSize
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(COLOR_BG)
            .padding(14.dp)
    ) {
        when {
            currentSize.width < 200.dp -> SmallContent(bookmarks)
            currentSize.height < 200.dp -> MediumContent(bookmarks)
            else -> LargeContent(bookmarks)
        }
    }
}

// ═══════════════════════════════════════════════════
// SMALL (2x2) — العدد + آخر إضافة
// ═══════════════════════════════════════════════════
@Composable
private fun SmallContent(bookmarks: List<Bookmark>) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        // ── العنوان ──
        Text(
            text = "أثر",
            style = TextStyle(
                color = ColorProvider(COLOR_PRIMARY),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── العدد الكبير ──
        Text(
            text = bookmarks.size.toString(),
            style = TextStyle(
                color = ColorProvider(COLOR_TEXT_DARK),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "ذكرى",
            style = TextStyle(
                color = ColorProvider(COLOR_TEXT_MUTED),
                fontSize = 12.sp
            )
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        // ── آخر إضافة ──
        val latest = bookmarks.firstOrNull()
        if (latest != null) {
            Text(
                text = "آخر إضافة:",
                style = TextStyle(
                    color = ColorProvider(COLOR_TEXT_MUTED),
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = latest.title.ifBlank { cleanUrl(latest.url) },
                style = TextStyle(
                    color = ColorProvider(COLOR_TEXT_DARK),
                    fontSize = 11.sp
                ),
                maxLines = 2
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// MEDIUM (4x2) — آخر 4 إضافات
// ═══════════════════════════════════════════════════
@Composable
private fun MediumContent(bookmarks: List<Bookmark>) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        // ── الهيدر ──
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أثر",
                style = TextStyle(
                    color = ColorProvider(COLOR_PRIMARY),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "• ${bookmarks.size} ذكرى",
                style = TextStyle(
                    color = ColorProvider(COLOR_TEXT_MUTED),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── آخر 4 إضافات ──
        if (bookmarks.isEmpty()) {
            Text(
                text = "لسه مفيش حاجة محفوظة",
                style = TextStyle(
                    color = ColorProvider(COLOR_TEXT_MUTED),
                    fontSize = 12.sp
                )
            )
        } else {
            bookmarks.take(4).forEach { bookmark ->
                Text(
                    text = "• ${bookmark.title.ifBlank { cleanUrl(bookmark.url) }}",
                    style = TextStyle(
                        color = ColorProvider(COLOR_TEXT_DARK),
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(5.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// LARGE (4x4) — آخر 6 إضافات + Footer
// ═══════════════════════════════════════════════════
@Composable
private fun LargeContent(bookmarks: List<Bookmark>) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        // ── الهيدر ──
        Text(
            text = "أثر",
            style = TextStyle(
                color = ColorProvider(COLOR_PRIMARY),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "${bookmarks.size} ذكرى محفوظة",
            style = TextStyle(
                color = ColorProvider(COLOR_TEXT_MUTED),
                fontSize = 11.sp
            )
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        // ── خط فاصل ──
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(COLOR_DIVIDER)
        ) {}

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── قائمة اللينكات ──
        if (bookmarks.isEmpty()) {
            Text(
                text = "لسه مفيش حاجة محفوظة\nشارك لينك مع أثر",
                style = TextStyle(
                    color = ColorProvider(COLOR_TEXT_MUTED),
                    fontSize = 12.sp
                )
            )
        } else {
            bookmarks.take(6).forEach { bookmark ->
                BookmarkItem(bookmark)
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // ── خط فاصل ──
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(COLOR_DIVIDER)
        ) {}

        Spacer(modifier = GlanceModifier.height(8.dp))

        // ── Footer ──
        Text(
            text = "افتح التطبيق →",
            style = TextStyle(
                color = ColorProvider(COLOR_PRIMARY),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * صف واحد للينك محفوظ
 */
@Composable
private fun BookmarkItem(bookmark: Bookmark) {
    val typeLabel = when (bookmark.type) {
        "youtube" -> "فيديو"
        "github" -> "كود"
        "twitter" -> "منشور"
        "medium" -> "مقال"
        "stackoverflow" -> "سؤال"
        else -> "مقال"
    }

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = bookmark.title.ifBlank { cleanUrl(bookmark.url) },
            style = TextStyle(
                color = ColorProvider(COLOR_TEXT_DARK),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Text(
            text = typeLabel,
            style = TextStyle(
                color = ColorProvider(COLOR_TEXT_MUTED),
                fontSize = 9.sp
            )
        )
    }
}

/**
 * تنظيف الـ URL — يشيل https:// و www.
 */
private fun cleanUrl(url: String): String {
    return url
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .trimEnd('/')
}