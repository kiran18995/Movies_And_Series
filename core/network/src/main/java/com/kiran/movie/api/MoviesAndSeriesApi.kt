package com.kiran.movie.api

import com.kiran.movie.core.network.BuildConfig
import com.kiran.movie.data.models.ItemResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesAndSeriesApi {
    companion object {
        const val BASE_URL = BuildConfig.BASE_URL
    }

    @GET("movie/popular?language=en-US")
    suspend fun getMovies(
        @Query("page") page: Int
    ): ItemResponse

    @GET("tv/popular?language=en-US")
    suspend fun getTvShows(
        @Query("page") page: Int
    ): ItemResponse

    @GET("search/movie?language=en-US")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int
    ): ItemResponse

    @GET("search/tv?language=en-US")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int
    ): ItemResponse
}
