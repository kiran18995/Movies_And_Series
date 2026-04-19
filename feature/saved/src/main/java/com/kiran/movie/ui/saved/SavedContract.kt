package com.kiran.movie.ui.saved

import com.kiran.movie.data.models.Item

class SavedContract {
    sealed class Event {
        object FetchBookmarks : Event()
        data class ToggleBookmark(val item: Item) : Event()
        data class ChangeTab(val isMovie: Boolean) : Event()
        data class Search(val query: String) : Event()
    }

    sealed class State {
        object Loading : State()
        data class Success(val items: List<Item>) : State()
        data class Error(val message: String) : State()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }
}
