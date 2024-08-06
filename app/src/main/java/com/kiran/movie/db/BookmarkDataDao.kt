package com.kiran.movie.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kiran.movie.data.models.Item

@Dao
interface BookmarkDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Query("SELECT * FROM bookmarkdatabase")
    suspend fun getAllBookmarks(): List<Item>

    @Query("DELETE FROM bookmarkdatabase WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("SELECT id FROM BookmarkDatabase")
    suspend fun getAllBookmarkedIds(): List<Int>

    @Query("SELECT * FROM BookmarkDatabase WHERE id = :itemId")
    suspend fun getBookmark(itemId: Int): Item?

    @Update
    suspend fun updateBookmark(item: Item)
}