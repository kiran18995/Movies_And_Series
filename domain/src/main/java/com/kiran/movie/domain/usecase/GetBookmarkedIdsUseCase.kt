package com.kiran.movie.domain.usecase

import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import javax.inject.Inject

class GetBookmarkedIdsUseCase @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) {
    suspend operator fun invoke(): List<Int> {
        return repository.getBookmarkedIds()
    }
}
