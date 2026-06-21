package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class GetUpcomingMoviesUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(
        language: String? = null,
        page: Int = 1,
    ): List<Item> = repository.getUpcomingMoviesByLanguage(language, page)
}
