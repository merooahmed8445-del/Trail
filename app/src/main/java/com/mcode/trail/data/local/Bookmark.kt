package com.mcode.trail.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val note: String,
    val type: String,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    // ✨ الجديد
    val isFavorite: Boolean = false
)