package com.plexviewer.adapter

import PlexServer
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plexviewer.databinding.ServerItemBinding

class ServerAdapter(
    private val context: Context,
    private val dataset: List<PlexServer>
): RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    inner class ServerViewHolder(val binding: ServerItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = ServerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val item = dataset[position]
        Log.d("LoginActivity", "Binding item at position $position: $item")
        holder.binding.serverName.text = item.deviceName
        holder.binding.serverProtocol.text = item.connectionProtocol
        holder.binding.serverAddress.text = item.address
        holder.binding.serverPort.text = item.port.toString()
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}