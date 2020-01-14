package de.datlag.hotdrop.fragment

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.hotdrop.MainActivity
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.DiscoverHost
import de.datlag.hotdrop.view.animation.CircularAnimation

class SearchDeviceFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var searchFAB: FloatingActionButton
    private var search = false
    private var animatable: Animatable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_device, container, false)
        initialize()
        initializeLogic()
        return rootView
    }

    fun setSearch(search: Boolean) {
        this.search = search
        if (this.search) {
            if(animatable != null) {
                animatable!!.start()
            }
            discoverHost.startDiscovery()
        } else {
            if(animatable != null && animatable!!.isRunning) {
                animatable!!.stop()
            }
            discoverHost.stopDiscovery()
        }
    }

    private fun initialize() {
        searchFAB = rootView.findViewById(R.id.fab_search)

        if(advancedActivity is MainActivity) {
            animatable = (advancedActivity as MainActivity).backgroundImage.drawable as Animatable
        }
    }

    private fun initializeLogic() {
        searchFAB.setOnClickListener { setSearch(!search) }
    }

    companion object {
        private lateinit var advancedActivity: AdvancedActivity
        private lateinit var discoverHost: DiscoverHost

        fun newInstance(activity: AdvancedActivity, discoverHost: DiscoverHost): SearchDeviceFragment {
            advancedActivity = activity
            Companion.discoverHost = discoverHost
            return SearchDeviceFragment()
        }
    }
}