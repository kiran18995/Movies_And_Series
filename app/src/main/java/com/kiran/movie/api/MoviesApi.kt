package com.kiran.movie.api

import com.kiran.movie.BuildConfig

interface MoviesApi {

    companion object {
        const val BASE_URL = BuildConfig.BASE_URL
    }
}