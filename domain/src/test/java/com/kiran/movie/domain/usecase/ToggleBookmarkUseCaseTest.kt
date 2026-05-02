package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleBookmarkUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val toggleBookmarkUseCase = ToggleBookmarkUseCase(repository)

    @Test
    fun `invoke should call toggleBookmark on repository`() = runTest {
        // Given
        val item = mockk<Item>()
        coEvery { repository.toggleBookmark(item) } returns Unit

        // When
        toggleBookmarkUseCase(item)

        // Then
        coVerify { repository.toggleBookmark(item) }
    }
}
