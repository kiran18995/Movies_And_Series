package com.kiran.movie.ui.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.paging.cachedIn
import com.kiran.movie.core.ui.models.TvCategory
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetTvShowsUseCase
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
class TvShowsViewModel @Inject constructor(
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedIdsUseCase: GetBookmarkedIdsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<TvShowsContract.State>(TvShowsContract.State.Loading)
    val state: StateFlow<TvShowsContract.State> = _state.asStateFlow()

    private val _effect = Channel<TvShowsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var currentQuery = ""
    private val searchQueryFlow = MutableStateFlow("")
    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TvCategory.POPULAR)
    val selectedCategory: StateFlow<TvCategory> = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    currentQuery = query
                    fetchSeries()
                }
        }
    }

    fun onEvent(event: TvShowsContract.Event) {
        when (event) {
            is TvShowsContract.Event.FetchSeries -> fetchSeries()
            is TvShowsContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is TvShowsContract.Event.RefreshBookmarks -> refreshBookmarks()
            is TvShowsContract.Event.Search -> {
                searchQueryFlow.value = event.query
            }
            is TvShowsContract.Event.SelectCategory -> {
                if (_selectedCategory.value != event.category) {
                    _selectedCategory.value = event.category
                    fetchSeries()
                }
            }
        }
    }

    private fun refreshBookmarks() {
        viewModelScope.launch {
            try {
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: Exception) {
                Log.e("TvShowsViewModel", "Failed to refresh bookmarks", e)
            }
        }
    }

    private fun fetchSeries() {
        viewModelScope.launch {
            try {
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
                val flow = getTvShowsUseCase(false, currentQuery, _selectedCategory.value.endpoint)
                    .cachedIn(viewModelScope)
                _state.value = TvShowsContract.State.Success(flow)
            } catch (e: Exception) {
                _state.value = TvShowsContract.State.Error(e.message ?: "An error occurred")
                _effect.send(TvShowsContract.Effect.ShowToast(e.message ?: "An error occurred"))
            }
        }
    }

    private fun toggleBookmark(item: com.kiran.movie.data.models.Item) {
        viewModelScope.launch {
            try {
                toggleBookmarkUseCase(item)
                _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            } catch (e: Exception) {
                _effect.send(TvShowsContract.Effect.ShowToast("Failed to toggle bookmark"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _effect.close()
    }
}