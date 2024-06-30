package com.kiran.movie.ui.saved

import androidx.lifecycle.ViewModel
import com.kiran.movie.db.BookmarkDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val db: BookmarkDatabase,
) : ViewModel()