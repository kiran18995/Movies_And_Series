package com.kiran.movie.domain.repository

data class AiSearchParameters(
    val title: String? = null,
    val genres: List<Int>? = null,
    val year: Int? = null,
    val isTvShow: Boolean = false,
    val aiResponse: String? = null
)

interface AiRepository {
    suspend fun parseSearchQuery(query: String): AiSearchParameters
}
