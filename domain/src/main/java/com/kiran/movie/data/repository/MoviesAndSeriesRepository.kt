package com.kiran.movie.data.repository

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import kotlinx.coroutines.flow.Flow

interface MoviesAndSeriesRepository {
    fun getMovies(query: String, language: String?, sortBy: String): Flow<PagingData<Item>>
    fun getTvShows(query: String, category: String = "popular"): Flow<PagingData<Item>>
    suspend fun getUpcomingMoviesByLanguage(language: String?, page: Int = 1): List<Item>
    suspend fun getMoviesList(category: String, page: Int = 1): List<Item>
    suspend fun getTvShowsList(category: String, page: Int = 1): List<Item>
    suspend fun isBookmarked(itemId: Int): Boolean
    suspend fun toggleBookmark(item: Item)
    suspend fun getBookmarkedIds(): List<Int>
    suspend fun getAllBookmark(): Flow<List<Item>>
    suspend fun getMovieDetails(movieId: Int): ItemDetails
    suspend fun getTvShowDetails(seriesId: Int): ItemDetails
}