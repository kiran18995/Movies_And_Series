package com.kiran.movie.domain.usecase

import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetItemDetailsUseCaseTest {

    private val repository: MoviesAndSeriesRepository = mockk()
    private val getItemDetailsUseCase = GetItemDetailsUseCase(repository)

    @Test
    fun `invoke with isMovie true should call getMovieDetails`() = runTest {
        // Given
        val id = 123
        val isMovie = true
        val expectedDetails = mockk<ItemDetails>()
        coEvery { repository.getMovieDetails(id) } returns expectedDetails

        // When
        val result = getItemDetailsUseCase(id, isMovie)

        // Then
        assertEquals(expectedDetails, result)
    }

    @Test
    fun `invoke with isMovie false should call getTvShowDetails`() = runTest {
        // Given
        val id = 456
        val isMovie = false
        val expectedDetails = mockk<ItemDetails>()
        coEvery { repository.getTvShowDetails(id) } returns expectedDetails

        // When
        val result = getItemDetailsUseCase(id, isMovie)

        // Then
        assertEquals(expectedDetails, result)
    }
}
