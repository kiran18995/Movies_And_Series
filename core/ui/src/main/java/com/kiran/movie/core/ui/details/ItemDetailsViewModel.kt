package com.kiran.movie.core.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.movie.data.models.ItemDetails
import com.kiran.movie.domain.usecase.GetItemDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val getItemDetailsUseCase: GetItemDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    fun fetchDetails(id: Int, isMovie: Boolean) {
        viewModelScope.launch {
            _state.value = DetailsState.Loading
            try {
                val details = getItemDetailsUseCase(id, isMovie)
                _state.value = DetailsState.Success(details)
            } catch (e: Exception) {
                _state.value = DetailsState.Error(e.message ?: "Failed to fetch details")
            }
        }
    }

    fun resetState() {
        _state.value = DetailsState.Loading
    }
}

sealed class DetailsState {
    object Loading : DetailsState()
    data class Success(val details: ItemDetails) : DetailsState()
    data class Error(val message: String) : DetailsState()
}
