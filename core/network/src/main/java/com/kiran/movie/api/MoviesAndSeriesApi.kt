package com.kiran.movie.api

import com.kiran.movie.core.network.BuildConfig
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.models.ItemResponse
import retrofit2.http.GET
import retrofit2.http.Path
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

    /**
     * Fetch movies by category (popular, top_rated, upcoming, now_playing).
     */
    @GET("movie/{category}?language=en-US")
    suspend fun getMoviesByCategory(
        @Path("category") category: String,
        @Query("page") page: Int
    ): ItemResponse

    /**
     * Fetch TV shows by category (popular, top_rated, airing_today, on_the_air).
     */
    @GET("tv/{category}?language=en-US")
    suspend fun getTvShowsByCategory(
        @Path("category") category: String,
        @Query("page") page: Int
    ): ItemResponse

    /**
     * Fetch movie details including credits and videos.
     */
    @GET("movie/{movie_id}?append_to_response=credits,videos&language=en-US")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int
    ): ItemDetails

    /**
     * Fetch TV show details including credits and videos.
     */
    @GET("tv/{series_id}?append_to_response=credits,videos,external_ids&language=en-US")
    suspend fun getTvShowDetails(
        @Path("series_id") seriesId: Int
    ): ItemDetails

    /**
     * Search for movies by title.
     */
    @GET("search/movie?language=en-US")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("include_adult") includeAdult: Boolean = false
    ): ItemResponse

    /**
     * Search for TV shows by title.
     */
    @GET("search/tv?language=en-US")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("include_adult") includeAdult: Boolean = false
    ): ItemResponse
}


