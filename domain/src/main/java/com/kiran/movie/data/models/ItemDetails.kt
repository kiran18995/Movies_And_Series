package com.kiran.movie.data.models

import com.google.gson.annotations.SerializedName

data class ItemDetails(
    @SerializedName("id") val id: Int,
    @SerializedName(value = "title", alternate = ["name"]) val title: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName(value = "release_date", alternate = ["first_air_date"]) val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?, // For movies
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?, // For TV
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?, // For TV
    @SerializedName("status") val status: String?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("credits") val credits: Credits?,
    @SerializedName("videos") val videos: Videos?,
    @SerializedName("imdb_id") val imdbId: String?,
    @SerializedName("external_ids") val externalIds: ExternalIds?,
    @SerializedName("seasons") val seasons: List<SeasonInfo>?
)

data class ExternalIds(
    @SerializedName("imdb_id") val imdbId: String?
)

/**
 * Per-season metadata returned in the TV details `seasons` array.
 * season_number 0 = Specials — excluded from picker.
 */
data class SeasonInfo(
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("name") val name: String?
)

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)


data class Credits(
    @SerializedName("cast") val cast: List<Cast>?
)

data class Cast(
    @SerializedName("name") val name: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class Videos(
    @SerializedName("results") val results: List<Video>?
)

data class Video(
    @SerializedName("name") val name: String,
    @SerializedName("key") val key: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String
)
