package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class GetTvShowsListUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(
        category: String = "popular",
        page: Int = 1,
        query: String = ""
    ): List<Item> = if (query.isEmpty()) {
        repository.getTvShowsList(category, page)
    } else {
        repository.searchTvShowsList(query, page)
    }
}
