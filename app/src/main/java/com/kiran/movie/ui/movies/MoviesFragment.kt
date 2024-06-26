package com.kiran.movie.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kiran.movie.data.models.Movie
import com.kiran.movie.databinding.FragmentMoviesBinding
import com.kiran.movie.utils.GridSpacingItemDecoration
import com.kiran.movie.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

@AndroidEntryPoint
class MoviesFragment : Fragment() {

    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MoviesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        viewModel.getMoviesList()
        setupObserver()
        return binding.root
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moviesList.collect {
                when (it) {
                    is Resource.Error -> {
                        MotionToast.darkToast(
                            requireActivity(),
                            "ERROR",
                            it.toString(),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            null
                        )
                    }

                    is Resource.Loading -> {
                        binding.progressBar.isVisible = true
                    }

                    is Resource.Success -> {
                        binding.progressBar.isVisible = false
                        setupViews(it.dataFetched)
                    }
                }
            }
        }
    }

    private fun setupViews(dataFetched: List<Movie>) {
        val spanCount = 2 // 2 columns
        val spacing = 15 // 15px
        val includeEdge = false
        binding.recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        binding.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount,
                spacing,
                includeEdge
            )
        )
        binding.recyclerView.adapter = MoviesAdapter(dataFetched)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}