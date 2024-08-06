package com.kiran.movie.ui.movies

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) : ViewModel() {

    private val _moviesList = MutableStateFlow<Resource<PagingData<Item>>>(Resource.Loading())
    val moviesList: MutableStateFlow<Resource<PagingData<Item>>> = _moviesList

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                val bookmarkedIds = repository.getBookmarkedIds()
                repository.getMovies(true)
                    .map { pagingData ->
                        pagingData.map { item ->
                            item.copy(isBookmarked = bookmarkedIds.contains(item.id))
                        }
                    }
                    .cachedIn(viewModelScope)
                    .collect { pagingData ->
                        _moviesList.value = Resource.Success(pagingData)
                    }
            } catch (e: Exception) {
                _moviesList.value = Resource.Error(e)
            }
        }
    }

    fun toggleBookmark(item: Item) {
        viewModelScope.launch {
            repository.toggleBookmark(item)
        }
    }
}