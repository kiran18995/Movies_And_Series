package com.kiran.movie.ui.movies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kiran.movie.data.models.Movie
import com.kiran.movie.databinding.ItemCardThumbnailBinding

class MoviesAdapter(private val itemList: List<Movie>) : RecyclerView.Adapter<MoviesAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: ItemCardThumbnailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCardThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.binding.itemImage.load("https://image.tmdb.org/t/p/w500${currentItem.posterPath}")
    }

    override fun getItemCount() = itemList.size
}