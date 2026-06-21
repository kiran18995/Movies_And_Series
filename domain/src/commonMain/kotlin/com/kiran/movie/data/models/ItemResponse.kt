package com.kiran.movie.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<Item>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
data class Item(
    @SerialName("adult") val adult: Boolean = false,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("id") val id: Int,
    @SerialName("original_language") val originalLanguage: String? = null,
    @SerialName("original_title") val originalTitle: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("popularity") val popularity: Double = 0.0,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("video") val video: Boolean = false,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    var isBookmarked: Boolean = false,
    var isMovie: Boolean = false,
) {
    val displayTitle: String get() = title ?: name ?: originalTitle ?: ""
}
