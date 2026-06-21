package com.kiran.movie.db

import com.kiran.movie.data.models.Item

/**
 * Convert a [BookmarkEntity] (Room DB row) back to a domain [Item].
 */
fun BookmarkEntity.toItem(): Item = Item(
    adult = adult,
    backdropPath = backdropPath,
    id = id,
    originalLanguage = originalLanguage,
    originalTitle = originalTitle,
    overview = overview,
    popularity = popularity,
    posterPath = posterPath,
    releaseDate = releaseDate,
    title = title,
    video = video,
    voteAverage = voteAverage,
    voteCount = voteCount,
    isBookmarked = isBookmarked,
    isMovie = isMovie,
)

/**
 * Convert a domain [Item] to a [BookmarkEntity] ready for Room insertion.
 */
fun Item.toBookmarkEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    adult = adult,
    backdropPath = backdropPath,
    originalLanguage = originalLanguage,
    originalTitle = originalTitle,
    overview = overview,
    popularity = popularity,
    posterPath = posterPath,
    releaseDate = releaseDate,
    title = title,
    video = video,
    voteAverage = voteAverage,
    voteCount = voteCount,
    isBookmarked = isBookmarked,
    isMovie = isMovie,
)
