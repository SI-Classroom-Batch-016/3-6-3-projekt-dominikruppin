package com.plexviewer.ui.movie

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plexviewer.MainActivity
import com.plexviewer.adapter.MovieAdapter
import com.plexviewer.api.PlexApiManager
import com.plexviewer.databinding.FragmentMovieBinding

class MovieFragment: Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentMovieBinding
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
        binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plexApiManager.getMovies()
        plexApiManager.movies.observe(viewLifecycleOwner) {
            val recyclerView = binding.moviesRecyclerView
            recyclerView.adapter = MovieAdapter(it)
        }


    }
}