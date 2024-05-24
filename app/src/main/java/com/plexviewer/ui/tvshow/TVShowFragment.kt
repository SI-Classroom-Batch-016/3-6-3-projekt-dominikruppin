package com.plexviewer.ui.tvshow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.plexviewer.MainActivity
import com.plexviewer.adapter.ShowAdapter
import com.plexviewer.api.PlexApiManager
import com.plexviewer.databinding.FragmentTvshowBinding

class TVShowFragment: Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentTvshowBinding
    private lateinit var plexApiManager: PlexApiManager

    // Laden die Instanz des PlexAPIManagers aus der MainActivity.
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
        binding = FragmentTvshowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Abrufen der Serien
        plexApiManager.getShows()
        // Sobald die Serien geladen sind (LiveData) setzen wir den Adapter
        plexApiManager.show.observe(viewLifecycleOwner) {
            val recyclerView = binding.showsRecyclerView
            recyclerView.adapter = ShowAdapter(it)
            binding.overTitle.text = "Verfügbare Serien"
        }


    }
}