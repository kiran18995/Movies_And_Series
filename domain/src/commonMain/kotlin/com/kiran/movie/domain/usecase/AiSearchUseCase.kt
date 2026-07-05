package com.kiran.movie.domain.usecase

import com.kiran.movie.domain.repository.AiRepository
import com.kiran.movie.domain.repository.AiSearchParameters

class AiSearchUseCase(private val aiRepository: AiRepository) {
    suspend operator fun invoke(query: String): AiSearchParameters {
        return aiRepository.parseSearchQuery(query)
    }
}
