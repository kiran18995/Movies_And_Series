package com.kiran.movie.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.paging.cachedIn
import com.kiran.movie.core.ui.models.MovieCategory
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedIdsUseCase: GetBookmarkedIdsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MoviesContract.State>(MoviesContract.State.Loading)
    val state: StateFlow<MoviesContract.State> = _state.asStateFlow()

    private val _effect = Channel<MoviesContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var currentQuery = ""
    private val searchQueryFlow = MutableStateFlow("")
    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.POPULAR)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    currentQuery = query
                    fetchMovies()
                }
        }
    }

    fun onEvent(event: MoviesContract.Event) {
        when (event) {
            is MoviesContract.Event.FetchMovies -> fetchMovies()
            is MoviesContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is MoviesContract.Event.RefreshBookmarks -> refreshBookmarks()
            is MoviesContract.Event.Search -> {
                searchQueryFlow.value = event.query
            }
            is MoviesContract.Event.SelectCategory -> {
                if (_selectedCategory.value != event.category) {
                    _selectedCategory.value = event.category
                    fetchMovies()
                }
            }
        }
    }

    private fun refreshBookmarks() {
        viewModelScope.launch {
            try {
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Failed to refresh bookmarks", e)
            }
        }
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
                val flow = getMoviesUseCase(true, currentQuery, _selectedCategory.value.endpoint)
                    .cachedIn(viewModelScope)
                _state.value = MoviesContract.State.Success(flow)
            } catch (e: Exception) {
                _state.value = MoviesContract.State.Error(e.message ?: "An error occurred")
                _effect.send(MoviesContract.Effect.ShowToast(e.message ?: "An error occurred"))
            }
        }
    }

    private fun toggleBookmark(item: com.kiran.movie.data.models.Item) {
        viewModelScope.launch {
            try {
                toggleBookmarkUseCase(item)
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
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