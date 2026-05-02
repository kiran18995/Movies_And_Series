package com.kiran.movie.domain.usecase

import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetBookmarkedIdsUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getBookmarkedIdsUseCase = GetBookmarkedIdsUseCase(repository)

    @Test
    fun `invoke should return list of bookmarked ids from repository`() = runTest {
        // Given
        val expectedIds = listOf(1, 2, 3)
        coEvery { repository.getBookmarkedIds() } returns expectedIds

        // When
        val result = getBookmarkedIdsUseCase()

        // Then
        assertEquals(expectedIds, result)
    }
}
