package com.kiran.movie.api

import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.models.ItemResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorMoviesAndSeriesApi(private val client: HttpClient) : MoviesAndSeriesApi {

    override suspend fun getMovies(page: Int): ItemResponse =
        client.get("movie/popular") {
            parameter("page", page)
        }.body()

    override suspend fun getTvShows(page: Int): ItemResponse =
        client.get("tv/popular") {
            parameter("page", page)
        }.body()

    override suspend fun getMoviesByCategory(category: String?, page: Int): ItemResponse =
        client.get("movie/${category ?: "popular"}") {
            parameter("page", page)
        }.body()

    override suspend fun discoverMovies(language: String?, sortBy: String, page: Int): ItemResponse =
        client.get("discover/movie") {
            parameter("page", page)
            parameter("sort_by", sortBy)
            if (!language.isNullOrBlank()) {
                parameter("with_original_language", language)
            }
        }.body()

    override suspend fun getUpcomingMoviesByLanguage(
        language: String?,
        releaseDate: String,
        page: Int,
    ): ItemResponse =
        client.get("discover/movie") {
            parameter("page", page)
            parameter("sort_by", "popularity.desc")
            parameter("primary_release_date.gte", releaseDate)
            if (!language.isNullOrBlank()) {
                parameter("with_original_language", language)
            }
        }.body()

    override suspend fun getTvShowsByCategory(category: String, page: Int): ItemResponse =
        client.get("tv/$category") {
            parameter("page", page)
        }.body()

    override suspend fun getMovieDetails(movieId: Int): ItemDetails =
        client.get("movie/$movieId") {
            parameter("append_to_response", "credits,videos,external_ids")
        }.body()

    override suspend fun getTvShowDetails(seriesId: Int): ItemDetails =
        client.get("tv/$seriesId") {
            parameter("append_to_response", "credits,videos,external_ids")
        }.body()

    override suspend fun searchMovies(query: String, page: Int, includeAdult: Boolean): ItemResponse =
        client.get("search/movie") {
            parameter("query", query)
            parameter("page", page)
            parameter("include_adult", includeAdult)
        }.body()

    override suspend fun searchTvShows(query: String, page: Int, includeAdult: Boolean): ItemResponse =
        client.get("search/tv") {
            parameter("query", query)
            parameter("page", page)
            parameter("include_adult", includeAdult)
        }.body()
}
