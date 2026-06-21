package com.kiran.movie.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetails(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("genres") val genres: List<Genre>? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("tagline") val tagline: String? = null,
    @SerialName("credits") val credits: Credits? = null,
    @SerialName("videos") val videos: Videos? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("external_ids") val externalIds: ExternalIds? = null,
    @SerialName("seasons") val seasons: List<SeasonInfo>? = null,
) {
    val displayTitle: String get() = title ?: name ?: ""
    val displayDate: String get() = releaseDate ?: firstAirDate ?: ""
}

@Serializable
data class Genre(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)

@Serializable
data class Credits(
    @SerialName("cast") val cast: List<Cast>? = null,
)

@Serializable
data class Cast(
    @SerialName("name") val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
)

@Serializable
data class Videos(
    @SerialName("results") val results: List<Video>? = null,
)

@Serializable
data class Video(
    @SerialName("name") val name: String,
    @SerialName("key") val key: String,
    @SerialName("site") val site: String,
    @SerialName("type") val type: String,
)

@Serializable
data class ExternalIds(
    @SerialName("imdb_id") val imdbId: String? = null,
)

@Serializable
data class SeasonInfo(
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_count") val episodeCount: Int,
    @SerialName("name") val name: String,
)
