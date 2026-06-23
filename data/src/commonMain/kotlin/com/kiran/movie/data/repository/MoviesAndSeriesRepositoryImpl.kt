package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import com.kiran.movie.db.BookmarkDatabase
import com.kiran.movie.db.toBookmarkEntity
import com.kiran.movie.db.toItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MoviesAndSeriesRepositoryImpl(
    private val moviesAndSeriesApi: MoviesAndSeriesApi,
    private val bookmarkDatabase: BookmarkDatabase,
) : MoviesAndSeriesRepository {

    private val dao = bookmarkDatabase.bookmarkedMovieDao()

    override fun getMovies(
        query: String,
        language: String?,
        sortBy: String,
    ): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 20,
            ),
            pagingSourceFactory = {
                MoviesAndSeriesDataSource(
                    moviesAndSeriesApi,
                    isMovie = true,
                    query,
                    language = language,
                    sortBy = sortBy,
                )
            },
        ).flow

    override fun getTvShows(
        query: String,
        category: String,
    ): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie = false, query, category) },
        ).flow

    override suspend fun toggleBookmark(item: Item) {
        val exists = dao.getBookmark(item.id) != null
        if (exists) {
            dao.deleteItem(item.id)
            item.isBookmarked = false
        } else {
            item.isBookmarked = true
            dao.insertItem(item.toBookmarkEntity())
        }
    }

    override suspend fun isBookmarked(itemId: Int): Boolean = dao.getBookmark(itemId) != null

    override suspend fun getAllBookmark(): Flow<List<Item>> =
        dao.getAllBookmarks().map { list -> list.map { it.toItem() } }

    override suspend fun getBookmarkedIds(): List<Int> = dao.getAllBookmarkedIds()

    override suspend fun getMovieDetails(movieId: Int): ItemDetails =
        moviesAndSeriesApi.getMovieDetails(movieId)

    override suspend fun getTvShowDetails(seriesId: Int): ItemDetails =
        moviesAndSeriesApi.getTvShowDetails(seriesId)

    override suspend fun getUpcomingMoviesByLanguage(
        language: String?,
        page: Int,
    ): List<Item> =
        try {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString() // Format: yyyy-MM-dd
            moviesAndSeriesApi
                .getUpcomingMoviesByLanguage(language, today, page)
                .results
                .map { it.apply { isMovie = true } }
        } catch (e: Exception) {
            println("getUpcomingMoviesByLanguage error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    override suspend fun discoverMoviesList(
        language: String?,
        sortBy: String,
        page: Int
    ): List<Item> =
        try {
            moviesAndSeriesApi.discoverMovies(language, sortBy, page)
                .results
                .map { it.apply { isMovie = true } }
        } catch (e: Exception) {
            println("discoverMoviesList error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    override suspend fun getMoviesList(category: String?, page: Int): List<Item> =
        try {
            moviesAndSeriesApi.getMoviesByCategory(category, page)
                .results
                .map { it.apply { isMovie = true } }
        } catch (e: Exception) {
            println("getMoviesList error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    override suspend fun getTvShowsList(category: String, page: Int): List<Item> =
        try {
            moviesAndSeriesApi.getTvShowsByCategory(category, page)
                .results
                .map { it.apply { isMovie = false } }
        } catch (e: Exception) {
            println("getTvShowsList error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    override suspend fun searchMoviesList(query: String, page: Int): List<Item> =
        try {
            moviesAndSeriesApi.searchMovies(query, page)
                .results
                .map { it.apply { isMovie = true } }
        } catch (e: Exception) {
            println("searchMoviesList error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    override suspend fun searchTvShowsList(query: String, page: Int): List<Item> =
        try {
            moviesAndSeriesApi.searchTvShows(query, page)
                .results
                .map { it.apply { isMovie = false } }
        } catch (e: Exception) {
            println("searchTvShowsList error: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
}
