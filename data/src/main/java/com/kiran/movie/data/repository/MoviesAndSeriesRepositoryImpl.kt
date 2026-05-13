package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import com.kiran.movie.db.BookmarkDatabase
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class MoviesAndSeriesRepositoryImpl
    @Inject
    constructor(
        private val moviesAndSeriesApi: MoviesAndSeriesApi,
        private val bookmarkDatabase: BookmarkDatabase,
    ) : MoviesAndSeriesRepository {
        private val dao = bookmarkDatabase.bookmarkedMovieDao()

        override fun getTvShows(
            query: String,
            category: String,
        ): Flow<PagingData<Item>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 2,
                        enablePlaceholders = false,
                        prefetchDistance = 20,
                    ),
                pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie = false, query, category) },
            ).flow

        override fun getMovies(
            query: String,
            language: String?,
            sortBy: String,
        ): Flow<PagingData<Item>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 2,
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

        override suspend fun toggleBookmark(item: Item) {
            bookmarkDatabase.withTransaction {
                val exists = dao.getBookmark(item.id) != null
                if (exists) {
                    dao.deleteItem(item.id)
                    item.isBookmarked = false
                } else {
                    item.isBookmarked = true
                    dao.insertItem(item)
                }
            }
        }

        override suspend fun isBookmarked(itemId: Int): Boolean = dao.getBookmark(itemId) != null

        override suspend fun getAllBookmark(): Flow<List<Item>> = dao.getAllBookmarks()

        override suspend fun getBookmarkedIds(): List<Int> = dao.getAllBookmarkedIds()

        override suspend fun getMovieDetails(movieId: Int): ItemDetails = moviesAndSeriesApi.getMovieDetails(movieId)

        override suspend fun getTvShowDetails(seriesId: Int): ItemDetails = moviesAndSeriesApi.getTvShowDetails(seriesId)

        override suspend fun getUpcomingMoviesByLanguage(
            language: String?,
            page: Int,
        ): List<Item> =
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(Calendar.getInstance().time)
                moviesAndSeriesApi.getUpcomingMoviesByLanguage(language, today, page).results.map { it.apply { isMovie = true } }
            } catch (_: Exception) {
                emptyList()
            }

        override suspend fun getMoviesList(
            category: String?,
            page: Int,
        ): List<Item> =
            try {
                moviesAndSeriesApi.getMoviesByCategory(category, page).results.map { it.apply { isMovie = true } }
            } catch (_: Exception) {
                emptyList()
            }

        override suspend fun getTvShowsList(
            category: String,
            page: Int,
        ): List<Item> =
            try {
                moviesAndSeriesApi.getTvShowsByCategory(category, page).results.map { it.apply { isMovie = false } }
            } catch (_: Exception) {
                emptyList()
            }
    }
