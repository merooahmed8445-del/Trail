package com.mcode.trail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mcode.trail.ui.bookmarks.BookmarkViewModel
import com.mcode.trail.ui.bookmarks.DetailScreen
import com.mcode.trail.ui.bookmarks.HomeScreen
import com.mcode.trail.ui.splash.SplashScreen
import com.mcode.trail.ui.theme.TrailTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✨ قراءة الـ bookmark_id من الـ Intent (Deep Link)
        val bookmarkId = if (intent?.action == "com.mcode.trail.OPEN_BOOKMARK") {
            intent.getLongExtra("bookmark_id", -1L).takeIf { it > 0 }
        } else null

        setContent {
            TrailRoot(initialBookmarkId = bookmarkId)
        }
    }
}

@Composable
fun TrailRoot(initialBookmarkId: Long? = null) {
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    val scope = rememberCoroutineScope()

    val savedDarkMode by themePrefs.isDarkMode.collectAsState(initial = null)
    val systemDark = isSystemInDarkTheme()
    val isDark = savedDarkMode ?: systemDark

    var showSplash by rememberSaveable { mutableStateOf(true) }

    TrailTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showSplash) {
                SplashScreen(onFinished = { showSplash = false })
            } else {
                TrailNav(
                    isDarkTheme = isDark,
                    onToggleTheme = { newValue ->
                        scope.launch { themePrefs.setDarkMode(newValue) }
                    },
                    initialBookmarkId = initialBookmarkId
                )
            }
        }
    }
}

@Composable
fun TrailNav(
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    initialBookmarkId: Long? = null,
    viewModel: BookmarkViewModel = viewModel()
) {
    var selectedBookmarkId by rememberSaveable {
        mutableStateOf<Long?>(initialBookmarkId)
    }
    val allBookmarks by viewModel.bookmarks.collectAsState()

    val currentBookmark = selectedBookmarkId?.let { id ->
        allBookmarks.find { it.id == id }
    }

    BackHandler(enabled = currentBookmark != null) {
        selectedBookmarkId = null
    }

    if (currentBookmark != null) {
        DetailScreen(
            bookmark = currentBookmark,
            onBack = { selectedBookmarkId = null },
            onUpdate = { viewModel.updateBookmark(it) },
            onDelete = {
                viewModel.deleteBookmark(it)
                selectedBookmarkId = null
            }
        )
    } else {
        HomeScreen(
            onBookmarkClick = { selectedBookmarkId = it.id },
            viewModel = viewModel,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}