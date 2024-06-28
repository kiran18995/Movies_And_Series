package com.kiran.movie.ui.tvshows

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
class TvShowsViewModel @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) : ViewModel() {

    private val _seriesList = MutableStateFlow<Resource<PagingData<Item>>>(Resource.Loading())
    val seriesList: MutableStateFlow<Resource<PagingData<Item>>> = _seriesList


    init {
        fetchSeries()
    }

    private fun fetchSeries() {
        viewModelScope.launch {
            try {
                repository.getTvShows(false).cachedIn(viewModelScope).collectLatest { pagingData ->
                    _seriesList.value = Resource.Success(pagingData.filter { true })
                }
            } catch (e: Exception) {
                _seriesList.value = Resource.Error(e)
            }
        }
    }
}