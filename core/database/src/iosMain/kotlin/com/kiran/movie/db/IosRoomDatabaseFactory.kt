package com.kiran.movie.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

/**
 * Creates a Room database for iOS using NSDocumentDirectory for persistent storage.
 */
fun createIosRoomDatabase(): BookmarkDatabase {
    val dbPath = NSHomeDirectory() + "/Documents/BookmarkDatabase.db"
    return Room.databaseBuilder<BookmarkDatabase>(
        name = dbPath,
    )
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
