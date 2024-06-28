package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesApi
import com.kiran.movie.data.models.Movie
import com.kiran.movie.data.paging.MoviesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesRepository @Inject constructor(private val moviesApi: MoviesApi) {

    fun getMovies(): Flow<PagingData<Movie>> {
        return Pager(config = PagingConfig(
            pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
        ), pagingSourceFactory = { MoviesDataSource(moviesApi) }).flow
    }
}