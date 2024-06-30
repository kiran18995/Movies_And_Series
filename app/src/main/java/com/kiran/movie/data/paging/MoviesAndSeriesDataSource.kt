package com.kiran.movie.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.models.Item

class MoviesAndSeriesDataSource(
    private val moviesAndSeriesApi: MoviesAndSeriesApi, private val isMovie: Boolean
) : PagingSource<Int, Item>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        try {
            val position = params.key ?: STARTING_PAGE_INDEX
            val response =
                if (isMovie) moviesAndSeriesApi.getMovies(position) else moviesAndSeriesApi.getTvShows(
                    position
                )
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