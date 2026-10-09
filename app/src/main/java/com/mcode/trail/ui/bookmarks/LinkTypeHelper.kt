package com.mcode.trail.ui.bookmarks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mcode.trail.ui.theme.ArticleColor
import com.mcode.trail.ui.theme.GitHubColor
import com.mcode.trail.ui.theme.MediumColor
import com.mcode.trail.ui.theme.StackOverflowColor
import com.mcode.trail.ui.theme.TwitterColor
import com.mcode.trail.ui.theme.YouTubeColor

object LinkTypeHelper {

    fun getIconAndColor(type: String): Pair<ImageVector, Color> {
        return when (type) {
            "github" -> Icons.Default.Code to GitHubColor
            "youtube" -> Icons.Default.PlayArrow to YouTubeColor
            "twitter" -> Icons.Default.Article to TwitterColor
            "medium" -> Icons.Default.Article to MediumColor
            "stackoverflow" -> Icons.Default.Article to StackOverflowColor
            else -> Icons.Default.Article to ArticleColor
        }
    }
}