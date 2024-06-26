package com.kiran.movie.data.repository

import com.kiran.movie.api.MoviesApi
import com.kiran.movie.utils.Resource
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MoviesRepository @Inject constructor(private val moviesApi: MoviesApi) {

    fun getQuotesGenres() = flow {
        try {
            val genres = moviesApi.getMovies(1)
            if (genres.results.isNotEmpty()) {
                emit(Resource.Success(genres.results))
            } else {
                emit(Resource.Error(Throwable("Unable to Make Request")))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}