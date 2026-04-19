package com.kiran.movie.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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

    init {
        onEvent(MoviesContract.Event.FetchMovies)
    }

    private var currentQuery = ""

    fun onEvent(event: MoviesContract.Event) {
        when (event) {
            is MoviesContract.Event.FetchMovies -> fetchMovies()
            is MoviesContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is MoviesContract.Event.RefreshBookmarks -> refreshBookmarks()
            is MoviesContract.Event.Search -> {
                currentQuery = event.query
                fetchMovies()
            }
        }
    }

    private fun refreshBookmarks() {
        viewModelScope.launch {
            try {
                bookmarkedIdsFlow.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private val bookmarkedIdsFlow = MutableStateFlow<Set<Int>>(emptySet())

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                bookmarkedIdsFlow.value = getBookmarkedIdsUseCase().toSet()
                val flow = getMoviesUseCase(true, currentQuery)
                    .cachedIn(viewModelScope)
                    .combine(bookmarkedIdsFlow) { pagingData, bookmarkedIds ->
                        pagingData.map { item ->
                            item.copy(isBookmarked = bookmarkedIds.contains(item.id))
                        }
                    }
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
                val currentSet = bookmarkedIdsFlow.value.toMutableSet()
                if (currentSet.contains(item.id)) {
                    currentSet.remove(item.id)
                } else {
                    currentSet.add(item.id)
                }
                bookmarkedIdsFlow.value = currentSet
                toggleBookmarkUseCase(item)
            } catch (e: Exception) {
                _effect.send(MoviesContract.Effect.ShowToast("Failed to toggle bookmark"))
                // Revert optimistic update on failure by fetching again
                bookmarkedIdsFlow.value = getBookmarkedIdsUseCase().toSet()
            }
        }
    }
}