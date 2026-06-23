package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class DiscoverMoviesListUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(
        language: String? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1,
        query: String = ""
    ): List<Item> = if (query.isEmpty()) {
        repository.discoverMoviesList(language, sortBy, page)
    } else {
        repository.searchMoviesList(query, page)
    }
}
