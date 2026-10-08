package com.mcode.trail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL

data class LinkMetadata(
    val title: String?,
    val imageUrl: String?
)

object TitleFetcher {

    suspend fun fetchMetadata(url: String): LinkMetadata = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) AppleWebKit/537.36")
                .timeout(8000)
                .get()

            val title = doc.title()
                .trim()
                .replace(Regex("\\s+"), " ")
                .takeIf { it.isNotBlank() && it.length <= 200 }

            val image = doc.selectFirst("meta[property=og:image]")?.attr("content")
                ?: doc.selectFirst("meta[name=twitter:image]")?.attr("content")
                ?: doc.selectFirst("meta[property=twitter:image]")?.attr("content")
                ?: doc.selectFirst("link[rel=image_src]")?.attr("href")

            LinkMetadata(
                title = title,
                imageUrl = image?.takeIf { it.startsWith("http") }
            )
        } catch (e: Exception) {
            LinkMetadata(title = null, imageUrl = null)
        }
    }

    fun getYouTubeThumbnail(url: String): String? {
        val videoId = extractYouTubeId(url) ?: return null
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    private fun extractYouTubeId(url: String): String? {
        return try {
            val patterns = listOf(
                Regex("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})"),
                Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
                Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
            )
            patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.get(1) }
        } catch (e: Exception) {
            null
        }
    }

    fun extractFromUrl(url: String): String {
        return try {
            val uri = URL(url)
            val host = uri.host.removePrefix("www.")
            val path = uri.path.trimEnd('/')

            when {
                host.contains("github.com") && path.count { it == '/' } >= 2 -> {
                    val parts = path.split("/").filter { it.isNotBlank() }
                    if (parts.size >= 2) "GitHub: ${parts[0]}/${parts[1]}" else host
                }
                host.contains("youtube.com") || host.contains("youtu.be") -> "YouTube Video"
                host.contains("twitter.com") || host.contains("x.com") -> "Twitter Post"
                host.contains("medium.com") -> "Medium Article"
                host.contains("stackoverflow.com") -> "Stack Overflow"
                else -> host
            }
        } catch (e: Exception) {
            url
        }
    }
}