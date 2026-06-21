package com.kiran.movie.domain.usecase

import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class GetBookmarkedIdsUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(): List<Int> = repository.getBookmarkedIds()
}
