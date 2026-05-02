package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllBookmarksUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getAllBookmarksUseCase = GetAllBookmarksUseCase(repository)

    @Test
    fun `invoke should return flow of bookmarked items from repository`() = runTest {
        // Given
        val expectedBookmarks = listOf(mockk<Item>(), mockk<Item>())
        coEvery { repository.getAllBookmark() } returns flowOf(expectedBookmarks)

        // When
        val result = getAllBookmarksUseCase()

        // Then
        result.collect { bookmarks ->
            assertEquals(expectedBookmarks, bookmarks)
        }
    }
}
