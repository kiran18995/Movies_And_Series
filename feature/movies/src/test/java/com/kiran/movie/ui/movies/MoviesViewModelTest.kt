package com.kiran.movie.ui.movies

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetUpcomingMoviesUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    private lateinit var viewModel: MoviesViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockGetMoviesUseCase = mockk<GetMoviesUseCase>()
    private val mockToggleBookmarkUseCase = mockk<ToggleBookmarkUseCase>()
    private val mockGetBookmarkedIdsUseCase = mockk<GetBookmarkedIdsUseCase>()
    private val mockGetUpcomingMoviesUseCase = mockk<GetUpcomingMoviesUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // GetMoviesUseCase.invoke is NOT suspend — use plain every/verify (issue #8)
        every { mockGetMoviesUseCase.invoke(any(), any(), any()) } returns flowOf(PagingData.empty())
        coEvery { mockGetBookmarkedIdsUseCase.invoke() } returns emptyList()
        coEvery { mockToggleBookmarkUseCase.invoke(any()) } returns Unit
        coEvery { mockGetUpcomingMoviesUseCase.invoke(any(), any()) } returns emptyList()

        viewModel = MoviesViewModel(
            mockGetMoviesUseCase,
            mockToggleBookmarkUseCase,
            mockGetBookmarkedIdsUseCase,
            mockGetUpcomingMoviesUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchDebouncing() = runTest {
        // Clear initial invocation from init block
        io.mockk.clearMocks(mockGetMoviesUseCase, answers = false)

        // Fire multiple search events rapidly
        viewModel.onEvent(MoviesContract.Event.Search("S"))
        viewModel.onEvent(MoviesContract.Event.Search("Sp"))
        viewModel.onEvent(MoviesContract.Event.Search("Spi"))
        viewModel.onEvent(MoviesContract.Event.Search("Spider"))

        // Advance time slightly, should not trigger fetch
        advanceTimeBy(100)
        // invoke is not suspend — use plain verify (issue #8)
        verify(exactly = 0) { mockGetMoviesUseCase.invoke(any(), any()) }

        // Advance time to pass debounce threshold
        advanceTimeBy(200)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that only the last query was executed (issue #5: no isMovie arg)
        verify(exactly = 1) { mockGetMoviesUseCase.invoke("Spider", any()) }
    }

    @Test
    fun testToggleBookmarkPessimisticUpdate() = runTest {
        val testItem = Item(
            id = 99,
            title = "Test Movie",
            isBookmarked = false,
            adult = false,
            backdropPath = null,
            originalLanguage = null,
            originalTitle = null,
            overview = null,
            popularity = 0.0,
            posterPath = null,
            releaseDate = null,
            video = false,
            voteAverage = 0.0,
            voteCount = 0
        )

        viewModel.onEvent(MoviesContract.Event.ToggleBookmark(testItem))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockToggleBookmarkUseCase.invoke(testItem) }
        coVerify { mockGetBookmarkedIdsUseCase.invoke() }
    }

    @Test
    fun `SelectLanguage should update language and trigger re-fetch`() = runTest {
        // Clear initial invocation
        io.mockk.clearMocks(mockGetMoviesUseCase, mockGetUpcomingMoviesUseCase, answers = false)

        val newLanguage = com.kiran.movie.core.ui.models.MovieLanguage.HINDI
        
        viewModel.onEvent(MoviesContract.Event.SelectLanguage(newLanguage))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify carousel refresh
        coVerify { mockGetUpcomingMoviesUseCase.invoke(newLanguage.code, any()) }
        // Verify movies refresh with new language
        verify { mockGetMoviesUseCase.invoke(any(), newLanguage.code, any()) }
    }

    @Test
    fun `SelectSortOrder should update sort order and trigger re-fetch`() = runTest {
        // Clear initial invocation
        io.mockk.clearMocks(mockGetMoviesUseCase, answers = false)

        val newSortOrder = com.kiran.movie.core.ui.models.MovieSortOrder.TOP_RATED
        
        viewModel.onEvent(MoviesContract.Event.SelectSortOrder(newSortOrder))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify movies refresh with new sort order
        verify { mockGetMoviesUseCase.invoke(any(), any(), newSortOrder.value) }
    }
}
