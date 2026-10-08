package com.mcode.trail.ui.bookmarks

object UrlUtils {

    fun detectType(url: String): String {
        return when {
            url.contains("github.com") -> "github"
            url.contains("youtube.com") || url.contains("youtu.be") -> "youtube"
            url.contains("twitter.com") || url.contains("x.com") -> "twitter"
            url.contains("medium.com") -> "medium"
            url.contains("stackoverflow.com") -> "stackoverflow"
            else -> "article"
        }
    }

    fun getDisplayName(type: String): String {
        return when (type) {
            "github" -> "GitHub"
            "youtube" -> "YouTube"
            "twitter" -> "Twitter / X"
            "medium" -> "Medium"
            "stackoverflow" -> "Stack Overflow"
            else -> "مقال"
        }
    }
    fun getTypeColor(type: String): androidx.compose.ui.graphics.Color {
        return when (type) {
            "github" -> com.mcode.trail.ui.theme.GitHubColor
            "youtube" -> com.mcode.trail.ui.theme.YouTubeColor
            "twitter" -> com.mcode.trail.ui.theme.TwitterColor
            "stackoverflow" -> com.mcode.trail.ui.theme.StackOverflowColor
            "medium" -> com.mcode.trail.ui.theme.MediumColor
            else -> com.mcode.trail.ui.theme.ArticleColor
        }
    }

    fun getShortUrl(url: String): String {
        return url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .take(60)
    }

    fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "الآن"
            minutes < 60 -> "منذ ${toArabicDigits(minutes)} دقيقة"
            hours < 24 -> "منذ ${toArabicDigits(hours)} ساعة"
            days < 7 -> "منذ ${toArabicDigits(days)} يوم"
            days < 30 -> "منذ ${toArabicDigits(days / 7)} أسبوع"
            else -> "منذ ${toArabicDigits(days / 30)} شهر"
        }
    }

    private fun toArabicDigits(number: Long): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { char ->
            if (char.isDigit()) arabicDigits[char - '0'] else char
        }.joinToString("")}}