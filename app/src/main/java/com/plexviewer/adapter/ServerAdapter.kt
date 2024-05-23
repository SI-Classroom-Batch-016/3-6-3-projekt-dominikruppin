package com.plexviewer.adapter

import PlexServer
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plexviewer.LoginActivity
import com.plexviewer.MainActivity
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
        Log.d("Serveradapter", "Binding item at position $position: $item")
        holder.binding.serverName.text = item.deviceName
        holder.binding.serverProtocol.text = "Protokoll: ${item.connectionProtocol}"
        holder.binding.serverAddress.text = item.address
        holder.binding.serverPort.text = "Port: ${item.port.toString()}"

        holder.binding.root.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("Plex", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("server_protocol", item.connectionProtocol)
            editor.putString("server_address", item.address)
            editor.putString("server_port", item.port.toString())
            editor.apply()
            Log.d("PlexAPIManager", "Ausgewählt wurde: $item")
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

}