package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class GetMoviesListUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(
        category: String? = "popular",
        page: Int = 1,
    ): List<Item> = repository.getMoviesList(category, page)
}
