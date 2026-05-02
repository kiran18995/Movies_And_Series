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

class GetTvShowsUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getTvShowsUseCase = GetTvShowsUseCase(repository)

    @Test
    fun `invoke should return paging data flow from repository`() = runTest {
        // Given
        val query = "Breaking Bad"
        val category = "popular"
        val expectedPagingData = PagingData.from(listOf(mockk<Item>()))
        every { repository.getTvShows(query, category) } returns flowOf(expectedPagingData)

        // When
        val result = getTvShowsUseCase(query, category)

        // Then
        result.collect { pagingData ->
            assertEquals(expectedPagingData, pagingData)
        }
    }
}
