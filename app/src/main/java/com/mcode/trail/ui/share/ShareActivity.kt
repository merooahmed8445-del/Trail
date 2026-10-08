package com.mcode.trail.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mcode.trail.TitleFetcher
import com.mcode.trail.ui.bookmarks.SaveScreen
import com.mcode.trail.ui.bookmarks.UrlUtils
import com.mcode.trail.ui.theme.TrailTheme
import kotlinx.coroutines.launch

class ShareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = extractUrlFromIntent(intent)

        if (sharedUrl.isNullOrBlank()) {
            finish()
            return
        }

        val viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[com.mcode.trail.ui.bookmarks.BookmarkViewModel::class.java]

        // ===== حالة الجلب =====
        var fetchedTitle by mutableStateOf<String?>(null)
        var fetchedImage by mutableStateOf<String?>(null)
        var isLoading by mutableStateOf(true)

        // نبدأ الجلب
        lifecycleScope.launch {
            // لو YouTube → نستخدم الطريقة السريعة
            val ytThumb = TitleFetcher.getYouTubeThumbnail(sharedUrl)
            if (ytThumb != null) {
                fetchedImage = ytThumb
            }

            // بعدها نجيب الميتاداتا كاملة
            val metadata = TitleFetcher.fetchMetadata(sharedUrl)
            fetchedTitle = metadata.title
            if (fetchedImage == null && metadata.imageUrl != null) {
                fetchedImage = metadata.imageUrl
            }
            isLoading = false
        }

        setContent {
            TrailTheme {
                val displayTitle = when {
                    isLoading && fetchedTitle.isNullOrBlank() -> "بيجيب العنوان..."
                    !fetchedTitle.isNullOrBlank() -> fetchedTitle!!
                    else -> TitleFetcher.extractFromUrl(sharedUrl)
                }

                SaveScreen(
                    url = sharedUrl,
                    title = displayTitle,
                    imageUrl = fetchedImage,
                    onSave = { note ->
                        viewModel.addBookmark(
                            url = sharedUrl,
                            title = if (fetchedTitle.isNullOrBlank()) {
                                TitleFetcher.extractFromUrl(sharedUrl)
                            } else {
                                fetchedTitle!!
                            },
                            note = note,
                            type = UrlUtils.detectType(sharedUrl),
                            imageUrl = fetchedImage
                        )
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun extractUrlFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null

        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val urlRegex = Regex("""https?://\S+""")
        return urlRegex.find(text)?.value ?: text.trim()
    }
}