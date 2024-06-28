package com.kiran.movie.api

import com.kiran.movie.BuildConfig
import com.kiran.movie.data.models.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApi {
    companion object {
        const val BASE_URL = BuildConfig.BASE_URL
    }

    @GET("movie/popular?language=en-US")
    suspend fun getMovies(

        @Query("page") page:Int
    ) : MovieResponse
}