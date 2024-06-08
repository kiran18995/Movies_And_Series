package com.kiran.movie.ui.tvshows

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TvShowsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is TV Shows Fragment"
    }
    val text: LiveData<String> = _text
}