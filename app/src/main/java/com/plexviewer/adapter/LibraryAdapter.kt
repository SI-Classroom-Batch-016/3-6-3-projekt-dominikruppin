package com.plexviewer.adapter

import Directory
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plexviewer.databinding.LibraryItemBinding

class LibraryAdapter(
    private val dataset: List<Directory>
): RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    inner class LibraryViewHolder(val binding: LibraryItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val binding = LibraryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LibraryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val item = dataset[position]
        holder.binding.libraryKey.text = item.key
        holder.binding.libraryName.text = item.title
        holder.binding.libraryType.text = item.type
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}