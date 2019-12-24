package de.datlag.hotdrop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.DiscoverHost
import de.datlag.hotdrop.view.animation.CircularAnimation

class SearchDeviceFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var searchFAB: FloatingActionButton
    private var search = false
    private lateinit var rotateAnimation: CircularAnimation
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_device, container, false)
        initialize()
        initializeLogic()
        return rootView
    }

    fun setSearch(search: Boolean) {
        this.search = search
        if (this.search) {
            rotateAnimation = CircularAnimation(searchFAB, 25F)
            rotateAnimation.duration = 10000
            rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
            rotateAnimation.repeatMode = Animation.RESTART
            rotateAnimation.repeatCount = Animation.INFINITE
            searchFAB.startAnimation(rotateAnimation)
            discoverHost.startDiscovery()
        } else {
            rotateAnimation.cancel()
            rotateAnimation.reset()
            searchFAB.clearAnimation()
            discoverHost.stopDiscovery()
        }
    }

    private fun initialize() {
        searchFAB = rootView.findViewById(R.id.fab_search)
    }

    private fun initializeLogic() {
        searchFAB.setOnClickListener { setSearch(!search) }
    }

    companion object {
        private lateinit var activity: AdvancedActivity
        private lateinit var discoverHost: DiscoverHost
        fun newInstance(activity: AdvancedActivity, discoverHost: DiscoverHost): SearchDeviceFragment {
            Companion.activity = activity
            Companion.discoverHost = discoverHost
            return SearchDeviceFragment()
        }
    }
}