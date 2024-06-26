package com.kiran.movie.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.movie.data.models.Movie
import com.kiran.movie.data.repository.MoviesRepository
import com.kiran.movie.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _moviesList = MutableStateFlow<Resource<List<Movie>>>(Resource.Loading())
    val moviesList get() = _moviesList

    private var movieList: List<Movie> = emptyList()

    init {
        viewModelScope.launch {
            _moviesList.emit(Resource.Loading())
        }
    }

    fun getMoviesList() = viewModelScope.launch {
        repository.getQuotesGenres().collect{
            _moviesList.emit(it)
            if (it is Resource.Success) {
                movieList = it.dataFetched
            }
        }
    }
}