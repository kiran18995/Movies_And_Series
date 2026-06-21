package com.kiran.movie.db

import androidx.room.RoomDatabase

/**
 * Finalises the Room database builder for Android by adding the correct migrations
 * and constructor, then builds it.
 *
 * Usage (from AppModule.kt):
 * ```kotlin
 * val builder = Room.databaseBuilder(context, BookmarkDatabase::class.java, "BookmarkDatabase")
 * val db = getRoomDatabase(builder)
 * ```
 */
fun getRoomDatabase(builder: RoomDatabase.Builder<BookmarkDatabase>): BookmarkDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .build()
}
