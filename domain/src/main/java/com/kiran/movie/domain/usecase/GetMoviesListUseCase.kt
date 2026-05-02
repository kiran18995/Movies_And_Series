package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import javax.inject.Inject

class GetMoviesListUseCase @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) {
    suspend operator fun invoke(category: String?, page: Int = 1): List<Item> {
        return repository.getMoviesList(category, page)
    }
}
