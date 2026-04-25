package com.kiran.movie.data.models

import com.google.gson.annotations.SerializedName

data class ItemDetails(
    val id: Int,
    @SerializedName(value = "title", alternate = ["name"]) val title: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("poster_path") val posterPath: String?,
    val overview: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    val genres: List<Genre>?,
    @SerializedName(value = "release_date", alternate = ["first_air_date"]) val releaseDate: String?,
    val runtime: Int?, // For movies
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?, // For TV
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?, // For TV
    val status: String?,
    val tagline: String?,
    val credits: Credits?,
    val videos: Videos?
)

data class Genre(
    val id: Int,
    val name: String
)


data class Credits(
    val cast: List<Cast>?
)

data class Cast(
    val name: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class Videos(
    val results: List<Video>?
)

data class Video(
    val name: String,
    val key: String,
    val site: String,
    val type: String
)
