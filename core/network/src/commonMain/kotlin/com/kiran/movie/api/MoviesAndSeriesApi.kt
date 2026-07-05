package com.kiran.movie.api

import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.models.ItemResponse

interface MoviesAndSeriesApi {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }

    suspend fun getMovies(page: Int): ItemResponse
    suspend fun getTvShows(page: Int): ItemResponse
    suspend fun getMoviesByCategory(category: String?, page: Int): ItemResponse
    suspend fun discoverMovies(language: String?, sortBy: String, page: Int): ItemResponse
    suspend fun discoverMoviesWithFilters(genres: String?, year: Int?, sortBy: String, page: Int): ItemResponse
    suspend fun discoverTvShowsWithFilters(genres: String?, year: Int?, sortBy: String, page: Int): ItemResponse
    suspend fun getUpcomingMoviesByLanguage(language: String?, releaseDate: String, page: Int): ItemResponse
    suspend fun getTvShowsByCategory(category: String, page: Int): ItemResponse
    suspend fun getMovieDetails(movieId: Int): ItemDetails
    suspend fun getTvShowDetails(seriesId: Int): ItemDetails
    suspend fun searchMovies(query: String, page: Int, includeAdult: Boolean = false): ItemResponse
    suspend fun searchTvShows(query: String, page: Int, includeAdult: Boolean = false): ItemResponse
}
