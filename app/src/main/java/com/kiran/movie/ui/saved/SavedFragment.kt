package com.kiran.movie.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

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
        adapter = SavedAdapter(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObserver()
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
                    }

                    is Resource.Success -> {
                        adapter.setItems(it.dataFetched)
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