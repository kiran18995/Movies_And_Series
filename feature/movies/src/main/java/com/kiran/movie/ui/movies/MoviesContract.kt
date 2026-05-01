package com.kiran.movie.ui.movies

import androidx.paging.PagingData
import com.kiran.movie.data.models.Item
import com.kiran.movie.core.ui.models.MovieLanguage
import com.kiran.movie.core.ui.models.MovieSortOrder
import kotlinx.coroutines.flow.Flow

class MoviesContract {
    sealed class Event {
        object FetchMovies : Event()
        object RefreshBookmarks : Event()
        data class ToggleBookmark(val item: Item) : Event()
        data class Search(val query: String) : Event()
        data class SelectLanguage(val language: MovieLanguage) : Event()
        data class SelectSortOrder(val sortOrder: MovieSortOrder) : Event()
    }

    sealed class State {
        object Loading : State()
        data class Success(val pagingDataFlow: Flow<PagingData<Item>>) : State()
        data class Error(val message: String) : State()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }
}

