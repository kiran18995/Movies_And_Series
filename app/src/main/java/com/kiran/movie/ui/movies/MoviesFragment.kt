package com.kiran.movie.ui.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kiran.movie.databinding.FragmentMoviesBinding
import com.kiran.movie.utils.GridSpacingItemDecoration
import com.kiran.movie.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoviesFragment : Fragment() {
    private var _binding: FragmentMoviesBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MoviesViewModel>()
    private lateinit var adapter: MoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesBinding.inflate(inflater, container, false)
        adapter = MoviesAdapter(viewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObserver()
        viewModel.fetchMovies()
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moviesList.collectLatest {
                when (it) {
                    is Resource.Error -> {
                        Toasty.error(
                            requireContext(),
                            "This is an error toast.",
                            Toast.LENGTH_SHORT,
                            true
                        ).show()
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        adapter.submitData(it.dataFetched)
                    }
                }
            }
        }
    }

    private fun setupViews() {
        val spanCount = 2 // 2 columns
        val spacing = 15 // 15px
        val includeEdge = false
        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(context, spanCount)
            recyclerView.addItemDecoration(
                GridSpacingItemDecoration(
                    spanCount, spacing, includeEdge
                )
            )
            recyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}