package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.domain.repository.AiSearchParameters

class AiDiscoverUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(params: AiSearchParameters, page: Int = 1): List<Item> {
        return repository.discoverMoviesWithFilters(
            genres = params.genres,
            year = params.year,
            title = params.title,
            isTvShow = params.isTvShow,
            page = page
        )
    }
}
