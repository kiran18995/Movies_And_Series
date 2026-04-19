package com.kiran.movie.core.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHint = MutableStateFlow("Search...")
    val searchHint: StateFlow<String> = _searchHint.asStateFlow()

    private val _isListEmpty = MutableStateFlow(false)
    val isListEmpty: StateFlow<Boolean> = _isListEmpty.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchHint(hint: String) {
        _searchHint.value = hint
    }

    fun updateIsListEmpty(isEmpty: Boolean) {
        _isListEmpty.value = isEmpty
    }
}
