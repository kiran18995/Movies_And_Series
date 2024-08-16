package com.kiran.movie.ui.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowsViewModel @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) : ViewModel() {

    private val _seriesList = MutableStateFlow<Resource<PagingData<Item>>>(Resource.Loading())
    val seriesList: MutableStateFlow<Resource<PagingData<Item>>> = _seriesList

    init {
        fetchSeries()
    }

    fun fetchSeries() {
        viewModelScope.launch {
            try {
                val bookmarkedIds = repository.getBookmarkedIds()
                repository.getTvShows(false)
                    .map { pagingData ->
                        pagingData.map { item ->
                            item.copy(isBookmarked = bookmarkedIds.contains(item.id))
                        }
                    }
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _seriesList.value = Resource.Success(pagingData)
                    }
            } catch (e: Exception) {
                _seriesList.value = Resource.Error(e)
            }
        }
    }

    fun toggleBookmark(item: Item) {
        viewModelScope.launch {
            repository.toggleBookmark(item)
        }
    }
}