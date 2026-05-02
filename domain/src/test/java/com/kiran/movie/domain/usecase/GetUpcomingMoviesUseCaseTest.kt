package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUpcomingMoviesUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getUpcomingMoviesUseCase = GetUpcomingMoviesUseCase(repository)

    @Test
    fun `invoke should return upcoming movies from repository`() = runTest {
        // Given
        val language = "en"
        val page = 1
        val expectedItems = listOf(mockk<Item>())
        coEvery { repository.getUpcomingMoviesByLanguage(language, page) } returns expectedItems

        // When
        val result = getUpcomingMoviesUseCase(language, page)

        // Then
        assertEquals(expectedItems, result)
        coVerify { repository.getUpcomingMoviesByLanguage(language, page) }
    }
}
