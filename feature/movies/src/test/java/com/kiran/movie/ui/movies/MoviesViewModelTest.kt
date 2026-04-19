package com.kiran.movie.ui.movies

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        coEvery { mockGetMoviesUseCase.invoke(any(), any()) } returns flowOf(PagingData.empty())
        coEvery { mockGetBookmarkedIdsUseCase.invoke() } returns emptyList()
        coEvery { mockToggleBookmarkUseCase.invoke(any()) } returns Unit

        viewModel = MoviesViewModel(
            mockGetMoviesUseCase,
            mockToggleBookmarkUseCase,
            mockGetBookmarkedIdsUseCase
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
        coVerify(exactly = 0) { mockGetMoviesUseCase.invoke(any(), any()) }

        // Advance time to pass debounce threshold
        advanceTimeBy(200)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that only the last query was executed
        coVerify(exactly = 1) { mockGetMoviesUseCase.invoke(true, "Spider") }
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
}
