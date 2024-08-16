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

    override suspend fun getTvShows(isMovie: Boolean): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie) }).flow
    }

    override suspend fun getMovies(isMovie: Boolean): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie) }).flow
    }

    override suspend fun toggleBookmark(item: Item) {
        if (item.isBookmarked) {
            dao.deleteItem(item.id)
            item.isBookmarked = false
        } else {
            dao.insertItem(item)
            item.isBookmarked = true
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