package com.kiran.movie.ui.movies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.kiran.movie.R
import com.kiran.movie.data.models.Item
import com.kiran.movie.databinding.ItemCardThumbnailBinding

class MoviesAdapter(private val shimmerLayoutItems: ShimmerFrameLayout) :
    PagingDataAdapter<Item, MoviesAdapter.MovieViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding =
            ItemCardThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = getItem(position)
        if (movie != null) {
            holder.bind(shimmerLayoutItems, movie)
        }
    }

    class MovieViewHolder(private val binding: ItemCardThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shimmerLayoutItems: ShimmerFrameLayout, item: Item) {
            shimmerLayoutItems.stopShimmer()
            showPlaceHolderShimmer()

            binding.itemImage.load("https://image.tmdb.org/t/p/w500${item.posterPath}") {
                transformations(RoundedCornersTransformation(25f))
                placeholder(showPlaceHolderShimmer())
            }
        }

        private fun showPlaceHolderShimmer(): ShimmerDrawable {
            val shimmer = Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(itemView.context, R.color.black_900))
                .setBaseAlpha(0.7f).setHighlightAlpha(0.7f)
                .setHighlightColor(ContextCompat.getColor(itemView.context, R.color.black_900))
                .setDuration(600).setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true)
                .build()
            return ShimmerDrawable().apply {
                setShimmer(shimmer)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}