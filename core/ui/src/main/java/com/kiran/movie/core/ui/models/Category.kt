package com.kiran.movie.core.ui.models

enum class MovieLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi"),
    KANNADA("kn", "Kannada"),
    TELUGU("te", "Telugu"),
    TAMIL("ta", "Tamil"),
    MALAYALAM("ml", "Malayalam")
}

enum class MovieSortOrder(val value: String, val displayName: String) {
    POPULAR("popularity.desc", "Popular"),
    TOP_RATED("vote_average.desc", "Top Rated"),
    NEW_TO_OLD("primary_release_date.desc", "New to Old"),
    OLD_TO_NEW("primary_release_date.asc", "Old to New")
}

/**
 * TV show categories available from the TMDB API.
 *
 * @property endpoint The TMDB API path segment (e.g., "popular", "top_rated").
 * @property displayName Human-readable label for UI display.
 */
enum class TvCategory(val endpoint: String, val displayName: String) {
    POPULAR("popular", "Popular"),
    TOP_RATED("top_rated", "Top Rated"),
    AIRING_TODAY("airing_today", "Airing Today"),
    ON_THE_AIR("on_the_air", "On The Air")
}
