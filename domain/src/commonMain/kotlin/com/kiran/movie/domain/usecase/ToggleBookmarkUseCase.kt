package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class ToggleBookmarkUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(item: Item) = repository.toggleBookmark(item)
}
