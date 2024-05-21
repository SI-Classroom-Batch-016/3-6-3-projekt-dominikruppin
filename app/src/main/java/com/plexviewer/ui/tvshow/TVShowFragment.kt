package com.plexviewer.ui.tvshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.plexviewer.databinding.FragmentTvshowBinding

class TVShowFragment : Fragment() {

    private lateinit var binding: FragmentTvshowBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val TVShowViewModel =
            ViewModelProvider(this).get(TVShowViewModel::class.java)

        binding = FragmentTvshowBinding.inflate(inflater, container, false)
        return binding.root
    }


}