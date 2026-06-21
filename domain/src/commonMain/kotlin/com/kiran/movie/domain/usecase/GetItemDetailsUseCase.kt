package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.repository.MoviesAndSeriesRepository

class GetItemDetailsUseCase(private val repository: MoviesAndSeriesRepository) {
    suspend operator fun invoke(id: Int, isMovie: Boolean): ItemDetails =
        if (isMovie) {
            repository.getMovieDetails(id)
        } else {
            repository.getTvShowDetails(id)
        }
}
