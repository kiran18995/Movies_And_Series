package com.kiran.movie.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
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
                repository.getMovies(true).cachedIn(viewModelScope).collectLatest { pagingData ->
                    _moviesList.value = Resource.Success(pagingData.filter { true })
                }
            } catch (e: Exception) {
                _moviesList.value = Resource.Error(e)
            }
        }
    }
}