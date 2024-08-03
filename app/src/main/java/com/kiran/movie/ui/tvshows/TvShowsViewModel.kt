package com.kiran.movie.ui.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepositoryImpl
import com.kiran.movie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowsViewModel @Inject constructor(
    private val repository: MoviesAndSeriesRepositoryImpl
) : ViewModel() {

    private val _seriesList = MutableStateFlow<Resource<PagingData<Item>>>(Resource.Loading())
    val seriesList: MutableStateFlow<Resource<PagingData<Item>>> = _seriesList

    init {
        fetchSeries()
    }

    private fun fetchSeries() {
        viewModelScope.launch {
            try {
                repository.getList(false).cachedIn(viewModelScope).collect { pagingData ->
                    _seriesList.value = Resource.Success(pagingData)
                }
            } catch (e: Exception) {
                _seriesList.value = Resource.Error(e)
            }
        }
    }
}