package com.kiran.movie.data.repository

import androidx.paging.PagingData
import com.kiran.movie.data.models.BookmarkedMovie
import com.kiran.movie.data.models.Item
import kotlinx.coroutines.flow.Flow

interface MoviesAndSeriesRepository {
    fun getList(isMovie: Boolean): Flow<PagingData<Item>>
    suspend fun insertBookmarkedMovie(movie: BookmarkedMovie)
    suspend fun deleteBookmarkedMovie(movieId: String)
    suspend fun getAllBookmarkedMovies(): List<BookmarkedMovie>
}