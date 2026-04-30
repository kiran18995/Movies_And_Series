package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import javax.inject.Inject

class GetItemDetailsUseCase @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) {
    suspend operator fun invoke(id: Int, isMovie: Boolean): ItemDetails {
        return if (isMovie) {
            repository.getMovieDetails(id)
        } else {
            repository.getTvShowDetails(id)
        }
    }
}
