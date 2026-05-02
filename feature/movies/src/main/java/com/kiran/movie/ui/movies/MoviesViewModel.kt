package com.kiran.movie.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.paging.cachedIn
import com.kiran.movie.core.ui.models.MovieLanguage
import com.kiran.movie.core.ui.models.MovieSortOrder
import com.kiran.movie.data.models.Item
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetUpcomingMoviesUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedIdsUseCase: GetBookmarkedIdsUseCase,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MoviesContract.State>(MoviesContract.State.Loading)
    val state: StateFlow<MoviesContract.State> = _state.asStateFlow()

    // Buffered so send() in init doesn't suspend before the UI starts collecting (issue #9)
    private val _effect = Channel<MoviesContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(MovieLanguage.ENGLISH)
    val selectedLanguage: StateFlow<MovieLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedSortOrder = MutableStateFlow(MovieSortOrder.POPULAR)
    val selectedSortOrder: StateFlow<MovieSortOrder> = _selectedSortOrder.asStateFlow()

    private val _carouselItems = MutableStateFlow<List<Item>>(emptyList())
    val carouselItems: StateFlow<List<Item>> = _carouselItems.asStateFlow()

    init {
        fetchCarouselItems(_selectedLanguage.value.code)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                // Fetch runs inside collectLatest so it is cancelled when a new query arrives (issue #7)
                .collectLatest { query ->
                    fetchMovies(query)
                }
        }
    }

    private fun fetchCarouselItems(language: String?) {
        viewModelScope.launch {
            try {
                val items = getUpcomingMoviesUseCase(language, 1)
                _carouselItems.value = items
            } catch (e: CancellationException) {
                throw e // Preserve structured concurrency (issue #11)
            } catch (e: Exception) {
                // Non-critical — carousel failure is silent
            }
        }
    }

    fun onEvent(event: MoviesContract.Event) {
        when (event) {
            is MoviesContract.Event.FetchMovies -> searchQueryFlow.value = searchQueryFlow.value // re-trigger
            is MoviesContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is MoviesContract.Event.RefreshBookmarks -> refreshBookmarks()
            is MoviesContract.Event.Search -> {
                searchQueryFlow.value = event.query
            }
            is MoviesContract.Event.SelectLanguage -> {
                if (_selectedLanguage.value != event.language) {
                    _selectedLanguage.value = event.language
                    fetchCarouselItems(event.language.code)
                    // Emit the current query again to re-trigger collectLatest with new language
                    searchQueryFlow.value = searchQueryFlow.value
                    // Force a re-fetch immediately since value hasn't changed (distinctUntilChanged would skip)
                    viewModelScope.launch { fetchMovies(searchQueryFlow.value) }
                }
            }
            is MoviesContract.Event.SelectSortOrder -> {
                if (_selectedSortOrder.value != event.sortOrder) {
                    _selectedSortOrder.value = event.sortOrder
                    // Emit the current query again to re-trigger collectLatest with new sort order
                    searchQueryFlow.value = searchQueryFlow.value
                    // Force a re-fetch immediately since value hasn't changed (distinctUntilChanged would skip)
                    viewModelScope.launch { fetchMovies(searchQueryFlow.value) }
                }
            }
        }
    }

    private fun refreshBookmarks() {
        viewModelScope.launch {
            try {
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: CancellationException) {
                throw e // issue #11
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Failed to refresh bookmarks", e)
            }
        }
    }

    // suspend so it executes in the caller's coroutine (collectLatest), enabling proper cancellation (issue #7)
    private suspend fun fetchMovies(query: String) {
        try {
            _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            val flow = getMoviesUseCase(query, _selectedLanguage.value.code, _selectedSortOrder.value.value) // issue #5: no isMovie
                .cachedIn(viewModelScope)
            _state.value = MoviesContract.State.Success(flow)
        } catch (e: CancellationException) {
            throw e // issue #11
        } catch (e: Exception) {
            _state.value = MoviesContract.State.Error(e.message ?: "An error occurred")
            _effect.send(MoviesContract.Effect.ShowToast(e.message ?: "An error occurred"))
        }
    }

    private fun toggleBookmark(item: com.kiran.movie.data.models.Item) {
        viewModelScope.launch {
            try {
                toggleBookmarkUseCase(item)
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: CancellationException) {
                throw e // issue #11
            } catch (e: Exception) {
                _effect.send(MoviesContract.Effect.ShowToast("Failed to toggle bookmark"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _effect.close()
    }
}