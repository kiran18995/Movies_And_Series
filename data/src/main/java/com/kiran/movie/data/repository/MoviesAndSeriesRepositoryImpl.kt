package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import com.kiran.movie.db.BookmarkDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesAndSeriesRepositoryImpl @Inject constructor(
    private val moviesAndSeriesApi: MoviesAndSeriesApi, bookmarkDatabase: BookmarkDatabase
) : MoviesAndSeriesRepository {
    private val dao = bookmarkDatabase.bookmarkedMovieDao()

    override fun getTvShows(isMovie: Boolean, query: String): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie, query) }).flow
    }

    override fun getMovies(isMovie: Boolean, query: String): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie, query) }).flow
    }

    override suspend fun toggleBookmark(item: Item) {
        val exists = dao.getBookmark(item.id) != null
        if (exists) {
            dao.deleteItem(item.id)
            item.isBookmarked = false
        } else {
            item.isBookmarked = true
            dao.insertItem(item)
        }
    }

    override suspend fun isBookmarked(itemId: Int): Boolean {
        return dao.getBookmark(itemId) != null
    }

    override suspend fun getAllBookmark() : Flow<List<Item>> {
        return dao.getAllBookmarks()
    }

    override suspend fun getBookmarkedIds(): List<Int> = dao.getAllBookmarkedIds()
}