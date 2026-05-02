package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMoviesListUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getMoviesListUseCase = GetMoviesListUseCase(repository)

    @Test
    fun `invoke should return list of items from repository`() = runTest {
        // Given
        val category = "popular"
        val page = 1
        val expectedItems = listOf(
            mockk<Item>(),
            mockk<Item>()
        )
        coEvery { repository.getMoviesList(category, page) } returns expectedItems

        // When
        val result = getMoviesListUseCase(category, page)

        // Then
        assertEquals(expectedItems, result)
    }
}
