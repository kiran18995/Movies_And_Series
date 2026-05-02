package com.kiran.movie.domain.usecase

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMoviesUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getMoviesUseCase = GetMoviesUseCase(repository)

    @Test
    fun `invoke should return paging data flow from repository`() = runTest {
        // Given
        val query = "Inception"
        val language = "en"
        val sortBy = "popularity.desc"
        val expectedPagingData = PagingData.from(listOf(mockk<Item>()))
        every { repository.getMovies(query, language, sortBy) } returns flowOf(expectedPagingData)

        // When
        val result = getMoviesUseCase(query, language, sortBy)

        // Then
        result.collect { pagingData ->
            assertEquals(expectedPagingData, pagingData)
        }
    }
}
