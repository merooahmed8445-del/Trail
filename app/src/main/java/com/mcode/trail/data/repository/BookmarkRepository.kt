package com.mcode.trail.data.repository

import com.mcode.trail.data.local.Bookmark
import com.mcode.trail.data.local.BookmarkDao
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val dao: BookmarkDao) {

    val allBookmarks: Flow<List<Bookmark>> = dao.getAllBookmarks()

    suspend fun insert(bookmark: Bookmark): Long = dao.insert(bookmark)

    suspend fun update(bookmark: Bookmark) = dao.update(bookmark)

    suspend fun delete(bookmark: Bookmark) = dao.delete(bookmark)

    suspend fun getById(id: Long): Bookmark? = dao.getBookmarkById(id)

    fun search(query: String): Flow<List<Bookmark>> = dao.searchBookmarks(query)
}