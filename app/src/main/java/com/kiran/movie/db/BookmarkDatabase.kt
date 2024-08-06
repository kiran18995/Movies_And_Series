package com.kiran.movie.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kiran.movie.data.models.Item

@Database(entities = [Item::class], version = 2, exportSchema = false)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkedMovieDao(): BookmarkDataDao
}