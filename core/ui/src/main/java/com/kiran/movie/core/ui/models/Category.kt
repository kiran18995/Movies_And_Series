package com.kiran.movie.core.ui.models

/**
 * Movie categories available from the TMDB API.
 *
 * @property endpoint The TMDB API path segment (e.g., "popular", "top_rated").
 * @property displayName Human-readable label for UI display.
 */
enum class MovieCategory(val endpoint: String, val displayName: String) {
    POPULAR("popular", "Popular"),
    TOP_RATED("top_rated", "Top Rated"),
    UPCOMING("upcoming", "Upcoming"),
    NOW_PLAYING("now_playing", "Now Playing")
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
