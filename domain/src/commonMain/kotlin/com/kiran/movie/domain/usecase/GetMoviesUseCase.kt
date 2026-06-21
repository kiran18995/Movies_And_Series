package com.kiran.movie.domain.usecase

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import kotlinx.coroutines.flow.Flow

class GetMoviesUseCase(private val repository: MoviesAndSeriesRepository) {
    operator fun invoke(
        query: String = "",
        language: String? = null,
        sortBy: String = "popularity.desc",
    ): Flow<PagingData<Item>> = repository.getMovies(query, language, sortBy)
}
