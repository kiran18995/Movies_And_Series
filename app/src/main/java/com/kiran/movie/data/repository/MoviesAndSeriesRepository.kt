package com.kiran.movie.data.repository

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import kotlinx.coroutines.flow.Flow

interface MoviesAndSeriesRepository {
    suspend fun getMovies(isMovie: Boolean): Flow<PagingData<Item>>
    suspend fun getTvShows(isMovie: Boolean): Flow<PagingData<Item>>
    suspend fun isBookmarked(itemId: Int): Boolean
    suspend fun toggleBookmark(item: Item)
    suspend fun getBookmarkedIds(): List<Int>
    suspend fun getAllBookmark(): Flow<List<Item>>
}