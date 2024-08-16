package com.kiran.movie.ui.saved

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.kiran.movie.BuildConfig
import com.kiran.movie.R
import com.kiran.movie.data.interfaces.BookmarkClickListener
import com.kiran.movie.data.models.Item
import com.kiran.movie.databinding.ItemCardThumbnailBinding

class SavedAdapter(private val viewModel: SavedViewModel) :
    ListAdapter<Item, SavedAdapter.MovieViewHolder>(DiffCallback()), BookmarkClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding =
            ItemCardThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val item = getItem(position) // Use getItem() provided by ListAdapter
        holder.bind(item)
    }

    inner class MovieViewHolder(
        private val binding: ItemCardThumbnailBinding,
        private val bookmarkClickListener: BookmarkClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.itemImage.load(BuildConfig.BASE_IMAGE_URL + item.posterPath) {
                transformations(RoundedCornersTransformation(25f))
                placeholder(showPlaceHolderShimmer())
            }
            binding.bookmark.setOnClickListener {
                bookmarkClickListener.onBookmarkClick(item, bindingAdapterPosition)
            }
            val bookmarkIcon = if (item.isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_un_bookmarked
            binding.bookmark.setImageResource(bookmarkIcon)
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

    override fun onBookmarkClick(item: Item, position: Int) {
        viewModel.toggleBookmark(item)
        item.isBookmarked = !item.isBookmarked
        notifyItemChanged(position)    }
}