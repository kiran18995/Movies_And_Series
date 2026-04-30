package com.kiran.movie.ui.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.paging.cachedIn
import com.kiran.movie.core.ui.models.TvCategory
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetTvShowsListUseCase
import com.kiran.movie.domain.usecase.GetTvShowsUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import com.kiran.movie.data.models.Item
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
class TvShowsViewModel @Inject constructor(
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedIdsUseCase: GetBookmarkedIdsUseCase,
    private val getTvShowsListUseCase: GetTvShowsListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<TvShowsContract.State>(TvShowsContract.State.Loading)
    val state: StateFlow<TvShowsContract.State> = _state.asStateFlow()

    // Buffered so send() in init doesn't suspend before the UI starts collecting (issue #9)
    private val _effect = Channel<TvShowsContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private val _bookmarkedIds = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TvCategory.POPULAR)
    val selectedCategory: StateFlow<TvCategory> = _selectedCategory.asStateFlow()

    private val _carouselItems = MutableStateFlow<List<Item>>(emptyList())
    val carouselItems: StateFlow<List<Item>> = _carouselItems.asStateFlow()

    init {
        fetchCarouselItems()
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                // Fetch runs inside collectLatest so it is cancelled when a new query arrives (issue #12)
                .collectLatest { query ->
                    fetchSeries(query)
                }
        }
    }

    private fun fetchCarouselItems() {
        viewModelScope.launch {
            try {
                val items = getTvShowsListUseCase("airing_today", 1)
                _carouselItems.value = items
            } catch (e: CancellationException) {
                throw e // Preserve structured concurrency (issue #11)
            } catch (e: Exception) {
                // Non-critical — carousel failure is silent
            }
        }
    }

    fun onEvent(event: TvShowsContract.Event) {
        when (event) {
            is TvShowsContract.Event.FetchSeries -> searchQueryFlow.value = searchQueryFlow.value
            is TvShowsContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is TvShowsContract.Event.RefreshBookmarks -> refreshBookmarks()
            is TvShowsContract.Event.Search -> {
                searchQueryFlow.value = event.query
            }
            is TvShowsContract.Event.SelectCategory -> {
                if (_selectedCategory.value != event.category) {
                    _selectedCategory.value = event.category
                    // Force immediate re-fetch with current query under new category (issue #12)
                    viewModelScope.launch { fetchSeries(searchQueryFlow.value) }
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
                Log.e("TvShowsViewModel", "Failed to refresh bookmarks", e)
            }
        }
    }

    // suspend so it executes in the caller's coroutine (collectLatest), enabling proper cancellation (issue #12)
    private suspend fun fetchSeries(query: String) {
        try {
            _bookmarkedIds.value = getBookmarkedIdsUseCase().toSet()
            val flow = getTvShowsUseCase(query, _selectedCategory.value.endpoint) // issue #5: no isMovie
                .cachedIn(viewModelScope)
            _state.value = TvShowsContract.State.Success(flow)
        } catch (e: CancellationException) {
            throw e // issue #11
        } catch (e: Exception) {
            _state.value = TvShowsContract.State.Error(e.message ?: "An error occurred")
            _effect.send(TvShowsContract.Effect.ShowToast(e.message ?: "An error occurred"))
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
                _effect.send(TvShowsContract.Effect.ShowToast("Failed to toggle bookmark"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _effect.close()
    }
}