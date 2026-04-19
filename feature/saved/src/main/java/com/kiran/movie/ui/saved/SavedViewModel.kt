package com.kiran.movie.ui.saved

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.movie.domain.usecase.GetAllBookmarksUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
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
class SavedViewModel @Inject constructor(
    private val getAllBookmarksUseCase: GetAllBookmarksUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SavedContract.State>(SavedContract.State.Loading)
    val state: StateFlow<SavedContract.State> = _state.asStateFlow()

    private val _effect = Channel<SavedContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private val _isMovieFilter = MutableStateFlow(true)
    val isMovieTab: StateFlow<Boolean> = _isMovieFilter.asStateFlow()
    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Debounce search at initialization — single pipeline, consistent with MoviesViewModel
            val debouncedSearch = searchQuery.debounce(300L).distinctUntilChanged()
            try {
                combine(getAllBookmarksUseCase(), _isMovieFilter, debouncedSearch) { items, isMovie, query ->
                    items.filter {
                        it.isMovie == isMovie &&
                            (query.isBlank() || it.title?.contains(query, ignoreCase = true) == true)
                    }
                }.collectLatest { filteredItems ->
                    _state.value = SavedContract.State.Success(filteredItems)
                }
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Failed to fetch bookmarks", e)
                _state.value = SavedContract.State.Error(e.message ?: "An error occurred")
                _effect.send(SavedContract.Effect.ShowToast(e.message ?: "An error occurred"))
            }
        }
    }

    fun onEvent(event: SavedContract.Event) {
        when (event) {
            is SavedContract.Event.FetchBookmarks -> { /* No-op: reactive pipeline handles this */ }
            is SavedContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is SavedContract.Event.ChangeTab -> _isMovieFilter.value = event.isMovie
            is SavedContract.Event.Search -> searchQuery.value = event.query
        }
    }

    private fun toggleBookmark(item: com.kiran.movie.data.models.Item) {
        viewModelScope.launch {
            try {
                toggleBookmarkUseCase(item)
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Failed to toggle bookmark", e)
                _effect.send(SavedContract.Effect.ShowToast("Failed to toggle bookmark"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _effect.close()
    }
}