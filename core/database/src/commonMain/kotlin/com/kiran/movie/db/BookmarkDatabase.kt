package com.kiran.movie.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = true)
@ConstructedBy(BookmarkDatabaseConstructor::class)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkedMovieDao(): BookmarkDataDao
}
