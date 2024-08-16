package com.kiran.movie.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.movie.data.models.Item
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: MoviesAndSeriesRepository
) : ViewModel() {

    private val _moviesList = MutableStateFlow<Resource<List<Item>>>(Resource.Loading())
    val moviesList: MutableStateFlow<Resource<List<Item>>> = _moviesList

    init {
        fetchBookmarks()
    }

    fun fetchBookmarks() {
        viewModelScope.launch {
            try {
                repository.getAllBookmark().collectLatest {
                    _moviesList.value = Resource.Success(it)
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