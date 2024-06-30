package com.kiran.movie.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiran.movie.data.models.BookmarkedMovie

@Dao
interface BookmarkDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(quote: BookmarkedMovie): Long

    @Query("SELECT * FROM bookmarked_movies")
    suspend fun getAllBookmarks(): List<BookmarkedMovie>

    @Query("DELETE FROM bookmarked_movies WHERE id = :id")
    suspend fun deleteItem(id: String)
}