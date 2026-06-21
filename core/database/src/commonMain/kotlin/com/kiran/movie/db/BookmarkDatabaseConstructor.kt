package com.kiran.movie.db

import androidx.room.RoomDatabaseConstructor

// KSP generates the actual implementations per platform.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object BookmarkDatabaseConstructor : RoomDatabaseConstructor<BookmarkDatabase>
