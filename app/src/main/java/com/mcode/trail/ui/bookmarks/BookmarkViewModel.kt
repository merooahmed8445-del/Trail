package com.mcode.trail.ui.bookmarks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mcode.trail.TrailApp
import com.mcode.trail.data.local.Bookmark
import com.mcode.trail.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookmarkRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val bookmarks: StateFlow<List<Bookmark>>

    init {
        val dao = (application as TrailApp).database.bookmarkDao()
        repository = BookmarkRepository(dao)

        bookmarks = repository.allBookmarks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
        }
    }

    fun updateBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.update(bookmark)
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.delete(bookmark)
        }
    }
}