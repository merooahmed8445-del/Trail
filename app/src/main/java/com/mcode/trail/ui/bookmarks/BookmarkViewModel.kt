package com.mcode.trail.ui.bookmarks

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcode.trail.data.backup.BackupManager
import com.mcode.trail.data.local.Bookmark
import com.mcode.trail.data.repository.BookmarkRepository
import com.mcode.trail.ui.widgets.TrailWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookmarkRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    // ✨ رسالة الـ Backup (للـ Toast/Snackbar)
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    private val allBookmarks: StateFlow<List<Bookmark>>
    val bookmarks: StateFlow<List<Bookmark>>

    init {
        val dao = (application as com.mcode.trail.TrailApp).database.bookmarkDao()
        repository = BookmarkRepository(dao)

        allBookmarks = repository.allBookmarks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bookmarks = combine(allBookmarks, _searchQuery, _showOnlyFavorites) { list, query, favOnly ->
            var filtered = list
            if (favOnly) filtered = filtered.filter { it.isFavorite }
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                filtered = filtered.filter { bookmark ->
                    bookmark.title.lowercase().contains(q) ||
                            bookmark.note.lowercase().contains(q) ||
                            bookmark.url.lowercase().contains(q)
                }
            }
            filtered
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    fun addBookmark(
        url: String,
        title: String,
        note: String,
        type: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.insert(
                Bookmark(
                    url = url,
                    title = title,
                    note = note,
                    type = type,
                    imageUrl = imageUrl
                )
            )
            TrailWidget().updateAll(getApplication())
        }
    }

    fun updateBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.update(bookmark)
            TrailWidget().updateAll(getApplication())
        }
    }

    fun toggleFavorite(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.update(bookmark.copy(isFavorite = !bookmark.isFavorite))
            TrailWidget().updateAll(getApplication())
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.delete(bookmark)
            TrailWidget().updateAll(getApplication())
        }
    }

    // ═══════════════════════════════════════════════════
    // Backup: Export / Import
    // ═══════════════════════════════════════════════════

    /**
     * يرجّع كل الذكريات كـ JSON String (للتصدير)
     */
    fun exportBookmarks(): String {
        return BackupManager.exportToJson(allBookmarks.value)
    }

    /**
     * يستورد ذكريات من JSON String
     */
    fun importBookmarks(json: String) {
        viewModelScope.launch {
            when (val result = BackupManager.importFromJson(json)) {
                is BackupManager.ImportResult.Success -> {
                    try {
                        repository.insertAll(result.bookmarks)
                        _backupMessage.value = "تم استيراد ${result.bookmarks.size} ذكرى ✓"
                        TrailWidget().updateAll(getApplication())
                    } catch (e: Exception) {
                        _backupMessage.value = "خطأ في الحفظ: ${e.message}"
                    }
                }
                is BackupManager.ImportResult.Error -> {
                    _backupMessage.value = result.message
                }
            }
        }
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }
}