package com.plexviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plexviewer.api.Movie
import com.plexviewer.databinding.MovieItemBinding

// Adapter zum setzen der Filme
class MovieAdapter(
    private val dataset: List<Movie>
): RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(val binding: MovieItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val item = dataset[position]
        holder.binding.movieName.text = item.title
        holder.binding.movieYear.text = item.year.toString()
        Glide.with(holder.binding.imageView.context)
            .load(item.coverImageUrl)
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}