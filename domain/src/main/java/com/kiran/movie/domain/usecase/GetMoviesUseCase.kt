package com.kiran.movie.domain.usecase

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) {
    operator fun invoke(isMovie: Boolean, query: String, category: String = "popular"): Flow<PagingData<Item>> {
        return repository.getMovies(isMovie, query, category)
    }
}
