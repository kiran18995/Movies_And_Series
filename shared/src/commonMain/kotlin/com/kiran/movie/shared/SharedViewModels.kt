package com.kiran.movie.shared

import com.kiran.movie.data.models.Item
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.domain.usecase.GetAllBookmarksUseCase
import com.kiran.movie.domain.usecase.GetItemDetailsUseCase
import com.kiran.movie.domain.usecase.GetMoviesListUseCase
import com.kiran.movie.domain.usecase.GetTvShowsListUseCase
import com.kiran.movie.domain.usecase.GetUpcomingMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the Home / Movies screen.
 * Swift observes [movies], [tvShows], [upcomingMovies] and [isLoading] via StateFlow.
 */
class HomeViewModel(
    private val getMoviesListUseCase: GetMoviesListUseCase,
    private val getTvShowsListUseCase: GetTvShowsListUseCase,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _movies = MutableStateFlow<List<Item>>(emptyList())
    val movies: StateFlow<List<Item>> = _movies.asStateFlow()

    private val _tvShows = MutableStateFlow<List<Item>>(emptyList())
    val tvShows: StateFlow<List<Item>> = _tvShows.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<Item>>(emptyList())
    val upcomingMovies: StateFlow<List<Item>> = _upcomingMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            _isLoading.value = true
            try {
                _movies.value = getMoviesListUseCase("popular")
                _tvShows.value = getTvShowsListUseCase("popular")
                _upcomingMovies.value = getUpcomingMoviesUseCase()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}

/**
 * Shared ViewModel for item details.
 */
class DetailViewModel(
    private val getItemDetailsUseCase: GetItemDetailsUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _details = MutableStateFlow<ItemDetails?>(null)
    val details: StateFlow<ItemDetails?> = _details.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDetails(id: Int, isMovie: Boolean) {
        scope.launch {
            _isLoading.value = true
            try {
                _details.value = getItemDetailsUseCase(id, isMovie)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}

/**
 * Shared ViewModel for saved/bookmarked items.
 */
class SavedViewModel(
    private val getAllBookmarksUseCase: GetAllBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _bookmarks = MutableStateFlow<List<Item>>(emptyList())
    val bookmarks: StateFlow<List<Item>> = _bookmarks.asStateFlow()

    init {
        scope.launch {
            getAllBookmarksUseCase().onEach { _bookmarks.value = it }.launchIn(scope)
        }
    }

    fun toggleBookmark(item: Item) {
        scope.launch { toggleBookmarkUseCase(item) }
    }

    fun dispose() {
        scope.cancel()
    }
}
