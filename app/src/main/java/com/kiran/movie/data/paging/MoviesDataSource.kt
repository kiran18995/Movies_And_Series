package com.kiran.movie.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kiran.movie.api.MoviesApi
import com.kiran.movie.data.models.Movie

class MoviesDataSource(
    private val moviesApi: MoviesApi
) : PagingSource<Int, Movie>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        try {
            val position = params.key ?: STARTING_PAGE_INDEX
            val response = moviesApi.getMovies(position)
            return LoadResult.Page(
                data = response.results,
                prevKey = null,
                nextKey = if (response.results.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}