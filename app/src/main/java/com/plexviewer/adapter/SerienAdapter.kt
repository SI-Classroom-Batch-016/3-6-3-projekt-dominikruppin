package com.plexviewer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plexviewer.api.Show
import com.plexviewer.databinding.ShowItemBinding

// Adapter zum setzen der Serien
class ShowAdapter(
    private val dataset: List<Show>
): RecyclerView.Adapter<ShowAdapter.ShowViewHolder>() {

    inner class ShowViewHolder(val binding: ShowItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val binding = ShowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        val item = dataset[position]
       holder.binding.showName.text = item.title
        holder.binding.showYear.text = item.year.toString()
        Glide.with(holder.binding.cover.context)
            .load(item.coverImageUrl)
            .into(holder.binding.cover)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}