package com.kiran.movie.ui.tvshows

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
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
import com.kiran.movie.ui.movies.MoviesAdapter

class SeriesAdapter(private val viewModel: TvShowsViewModel) :
    PagingDataAdapter<Item, SeriesAdapter.SeriesViewHolder>(MoviesAdapter.DiffCallback()),
    BookmarkClickListener {

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        val series = getItem(position)
        if (series != null) {
            holder.bind(series)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val binding =
            ItemCardThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeriesViewHolder(binding, this)
    }

    class SeriesViewHolder(
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
            val bookmarkIcon =
                if (item.isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_un_bookmarked
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

    override fun onBookmarkClick(item: Item, position: Int) {
        viewModel.toggleBookmark(item)
        item.isBookmarked = !item.isBookmarked
        notifyItemChanged(position)
    }
}