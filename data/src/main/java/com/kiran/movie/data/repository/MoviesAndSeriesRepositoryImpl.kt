package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import com.kiran.movie.db.BookmarkDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import androidx.room.withTransaction

class MoviesAndSeriesRepositoryImpl @Inject constructor(
    private val moviesAndSeriesApi: MoviesAndSeriesApi,
    private val bookmarkDatabase: BookmarkDatabase
) : MoviesAndSeriesRepository {
    private val dao = bookmarkDatabase.bookmarkedMovieDao()

    override fun getTvShows(isMovie: Boolean, query: String, category: String): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie, query, category) }).flow
    }

    override fun getMovies(isMovie: Boolean, query: String, category: String): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie, query, category) }).flow
    }

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

    override suspend fun isBookmarked(itemId: Int): Boolean {
        return dao.getBookmark(itemId) != null
    }

    override suspend fun getAllBookmark() : Flow<List<Item>> {
        return dao.getAllBookmarks()
    }

    override suspend fun getBookmarkedIds(): List<Int> = dao.getAllBookmarkedIds()

    override suspend fun getMovieDetails(movieId: Int): ItemDetails {
        return moviesAndSeriesApi.getMovieDetails(movieId)
    }

    override suspend fun getTvShowDetails(seriesId: Int): ItemDetails {
        return moviesAndSeriesApi.getTvShowDetails(seriesId)
    }

    override suspend fun getMoviesList(category: String, page: Int): List<Item> {
        return try {
            moviesAndSeriesApi.getMoviesByCategory(category, page).results.map { it.apply { isMovie = true } }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTvShowsList(category: String, page: Int): List<Item> {
        return try {
            moviesAndSeriesApi.getTvShowsByCategory(category, page).results.map { it.apply { isMovie = false } }
        } catch (e: Exception) {
            emptyList()
        }
    }
}