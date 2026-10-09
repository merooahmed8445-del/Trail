package com.mcode.trail.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Bookmark::class],
    version = 3,                    // ← من 2 لـ 3
    exportSchema = false
)
abstract class TrailDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: TrailDatabase? = null

        fun getDatabase(context: Context): TrailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrailDatabase::class.java,
                    "trail_database"
                )
                    .fallbackToDestructiveMigration()  // ← ✨ جديد (dev only)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}