package de.datlag.hotdrop.fragment

import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.hotdrop.MainActivity
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity

class SearchDeviceFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var searchFAB: FloatingActionButton
    private var search = false
    private var animatable: Animatable? = null
    private var listener: SearchCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_device, container, false)
        initialize()
        initializeLogic()
        return rootView
    }

    fun setSearch(search: Boolean) {
        this.search = search
        if (this.search) {
            animatable?.start()
            listener?.onSearchChanged(true)
        } else {
            animatable?.let {
                if (it.isRunning)
                    it.stop()
            }
            listener?.onSearchChanged(false)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is SearchCallback) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement SearchCallback")
        }
    }

    interface SearchCallback {
        fun onSearchChanged(search: Boolean)
    }

    companion object {
        private lateinit var advancedActivity: AdvancedActivity

        fun newInstance(activity: AdvancedActivity): SearchDeviceFragment {
            advancedActivity = activity
            return SearchDeviceFragment()
        }
    }
}