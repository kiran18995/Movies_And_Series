package com.kiran.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.paging.MoviesAndSeriesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesAndSeriesRepository @Inject constructor(private val moviesAndSeriesApi: MoviesAndSeriesApi) {

    fun getMovies(isMovie: Boolean): Flow<PagingData<Item>> {
        return Pager(config = PagingConfig(
            pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
        ), pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie) }).flow
    }

    fun getTvShows(isMovie: Boolean): Flow<PagingData<Item>> {
        return Pager(config = PagingConfig(
            pageSize = 2, enablePlaceholders = false, prefetchDistance = 20,
        ), pagingSourceFactory = { MoviesAndSeriesDataSource(moviesAndSeriesApi, isMovie) }).flow
    }
}