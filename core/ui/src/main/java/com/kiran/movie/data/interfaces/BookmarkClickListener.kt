package com.kiran.movie.data.interfaces

import com.kiran.movie.data.models.Item

interface BookmarkClickListener {
    fun onBookmarkClick(item: Item, position: Int)
}