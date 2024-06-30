package com.kiran.movie.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kiran.movie.data.models.BookmarkedMovie

@Database(entities = [BookmarkedMovie::class], version = 1)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkedMovieDao(): BookmarkDataDao
}