package com.kiran.movie.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kiran.movie.data.interfaces.BookmarkClickListener
import com.kiran.movie.data.models.Item
import com.kiran.movie.databinding.FragmentSavedBinding
import com.kiran.movie.utils.GridSpacingItemDecoration
import com.kiran.movie.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedFragment : Fragment(), BookmarkClickListener {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SavedViewModel>()
    private lateinit var adapter: SavedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        adapter = SavedAdapter(viewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObserver()
        viewModel.fetchBookmarks()
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
                        if (it.dataFetched.isEmpty()) {
                            binding.noBookmarkIcon.visibility = View.VISIBLE
                            binding.noData.visibility = View.VISIBLE
                        } else {
                            binding.noBookmarkIcon.visibility = View.GONE
                            binding.noData.visibility = View.GONE
                        }
                        adapter.submitList(it.dataFetched)
                    }
                }
            }
        }    }

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

    override fun onBookmarkClick(item: Item, position: Int) {
        viewModel.toggleBookmark(item)
    }
}