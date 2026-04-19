package com.kiran.movie.ui.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetTvShowsUseCase
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
class TvShowsViewModel @Inject constructor(
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val getBookmarkedIdsUseCase: GetBookmarkedIdsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<TvShowsContract.State>(TvShowsContract.State.Loading)
    val state: StateFlow<TvShowsContract.State> = _state.asStateFlow()

    private val _effect = Channel<TvShowsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        onEvent(TvShowsContract.Event.FetchSeries)
    }

    private var currentQuery = ""

    fun onEvent(event: TvShowsContract.Event) {
        when (event) {
            is TvShowsContract.Event.FetchSeries -> fetchSeries()
            is TvShowsContract.Event.ToggleBookmark -> toggleBookmark(event.item)
            is TvShowsContract.Event.RefreshBookmarks -> refreshBookmarks()
            is TvShowsContract.Event.Search -> {
                currentQuery = event.query
                fetchSeries()
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

    private fun fetchSeries() {
        viewModelScope.launch {
            try {
                bookmarkedIdsFlow.value = getBookmarkedIdsUseCase().toSet()
                val flow = getTvShowsUseCase(false, currentQuery)
                    .cachedIn(viewModelScope)
                    .combine(bookmarkedIdsFlow) { pagingData, bookmarkedIds ->
                        pagingData.map { item ->
                            item.copy(isBookmarked = bookmarkedIds.contains(item.id))
                        }
                    }
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
                val currentSet = bookmarkedIdsFlow.value.toMutableSet()
                if (currentSet.contains(item.id)) {
                    currentSet.remove(item.id)
                } else {
                    currentSet.add(item.id)
                }
                bookmarkedIdsFlow.value = currentSet
                toggleBookmarkUseCase(item)
            } catch (e: Exception) {
                _effect.send(TvShowsContract.Effect.ShowToast("Failed to toggle bookmark"))
                bookmarkedIdsFlow.value = getBookmarkedIdsUseCase().toSet()
            }
        }
    }
}