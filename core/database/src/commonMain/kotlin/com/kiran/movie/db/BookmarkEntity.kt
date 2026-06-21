package com.kiran.movie.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity mirroring the fields stored from [com.kiran.movie.data.models.Item].
 * Kept flat to avoid complex TypeConverters on iOS.
 */
@Entity(tableName = "BookmarkDatabase")
data class BookmarkEntity(
    @PrimaryKey val id: Int,
    val adult: Boolean = false,
    val backdropPath: String? = null,
    val originalLanguage: String? = null,
    val originalTitle: String? = null,
    val overview: String? = null,
    val popularity: Double = 0.0,
    val posterPath: String? = null,
    val releaseDate: String? = null,
    val title: String? = null,
    val video: Boolean = false,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val isBookmarked: Boolean = true,
    val isMovie: Boolean = false,
)
