package com.kiran.movie.domain.usecase

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import kotlinx.coroutines.flow.Flow

class GetTvShowsUseCase(private val repository: MoviesAndSeriesRepository) {
    operator fun invoke(
        query: String = "",
        category: String = "popular",
    ): Flow<PagingData<Item>> = repository.getTvShows(query, category)
}
