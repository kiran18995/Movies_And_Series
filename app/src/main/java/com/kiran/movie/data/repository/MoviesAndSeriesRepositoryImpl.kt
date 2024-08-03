package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.BookmarkedMovie
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import com.kiran.movie.db.BookmarkDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesAndSeriesRepositoryImpl @Inject constructor(
    private val moviesAndSeriesApi: MoviesAndSeriesApi,
    private val bookmarkDatabase: BookmarkDatabase
) : MoviesAndSeriesRepository {

    override fun getList(isMovie: Boolean): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 20,
            ),
            pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie) }
        ).flow
    }

    override suspend fun insertBookmarkedMovie(movie: BookmarkedMovie) {
        bookmarkDatabase.bookmarkedMovieDao().insertItem(movie)
    }

    override suspend fun deleteBookmarkedMovie(movieId: String) {
        bookmarkDatabase.bookmarkedMovieDao().deleteItem(movieId)
    }

    override suspend fun getAllBookmarkedMovies(): List<BookmarkedMovie> {
        return bookmarkDatabase.bookmarkedMovieDao().getAllBookmarks()
    }
}