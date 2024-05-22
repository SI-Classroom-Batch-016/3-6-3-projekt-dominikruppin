package com.plexviewer.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.plexviewer.MainActivity
import com.plexviewer.adapter.LibraryAdapter
import com.plexviewer.api.PlexApiManager
import com.plexviewer.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private val viewModel: PlexApiManager by activityViewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var plexApiManager: PlexApiManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        plexApiManager = mainActivity.plexApiManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plexApiManager.getLibraries()
        plexApiManager.libraries.observe(viewLifecycleOwner) {
            val recyclerView = binding.libraryRecyclerView
            recyclerView.adapter = LibraryAdapter(it)
        }


    }
}