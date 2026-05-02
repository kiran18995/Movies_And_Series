package com.kiran.movie.data.repository

import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemResponse
import com.kiran.movie.db.BookmarkDatabase
import com.kiran.movie.db.BookmarkDataDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MoviesAndSeriesRepositoryImplTest {

    private val api: MoviesAndSeriesApi = mockk()
    private val db: BookmarkDatabase = mockk()
    private val dao: BookmarkDataDao = mockk()
    private lateinit var repository: MoviesAndSeriesRepositoryImpl

    @Before
    fun setup() {
        every { db.bookmarkedMovieDao() } returns dao
        repository = MoviesAndSeriesRepositoryImpl(api, db)
    }

    private fun createTestItem(id: Int, title: String) = Item(
        id = id,
        title = title,
        adult = false,
        backdropPath = null,
        originalLanguage = "en",
        originalTitle = title,
        overview = "Overview",
        popularity = 1.0,
        posterPath = null,
        releaseDate = "2026-05-02",
        video = false,
        voteAverage = 8.0,
        voteCount = 100
    )

    @Test
    fun `getUpcomingMoviesByLanguage should return items from api`() = runTest {
        // Given
        val language = "en"
        val page = 1
        val item = createTestItem(1, "Upcoming Movie")
        val response = ItemResponse(page = 1, results = listOf(item), totalPages = 1, totalResults = 1)
        
        coEvery { api.getUpcomingMoviesByLanguage(language, any(), page) } returns response

        // When
        val result = repository.getUpcomingMoviesByLanguage(language, page)

        // Then
        assertEquals(1, result.size)
        assertEquals("Upcoming Movie", result[0].title)
        assertEquals(true, result[0].isMovie)
    }

    @Test
    fun `getUpcomingMoviesByLanguage should return empty list on exception`() = runTest {
        // Given
        coEvery { api.getUpcomingMoviesByLanguage(any(), any(), any()) } throws Exception("Network error")

        // When
        val result = repository.getUpcomingMoviesByLanguage("en", 1)

        // Then
        assertEquals(0, result.size)
    }
}
