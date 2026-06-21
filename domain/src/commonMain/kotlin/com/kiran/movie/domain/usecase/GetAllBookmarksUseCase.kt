package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import kotlinx.coroutines.flow.Flow

class GetAllBookmarksUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(): Flow<List<Item>> = repository.getAllBookmark()
}
