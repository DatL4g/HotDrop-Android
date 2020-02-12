package de.datlag.hotdrop.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.datlag.hotdrop.R
import de.datlag.hotdrop.p2p.Host
import de.datlag.hotdrop.view.adapter.ChooseHostRecyclerAdapter
import de.datlag.hotdrop.view.adapter.ChooseHostRecyclerAdapter.ItemClickListener

class ChooseDeviceFragment : Fragment(), ItemClickListener {
    private lateinit var rootView: View
    private lateinit var adapter: ChooseHostRecyclerAdapter
    private lateinit var recyclerView: RecyclerView
    private  var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_choose_device, container, false)
        initialize()
        initializeLogic()
        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun setHosts(hosts: Set<Host>) {
        mHosts = hosts
        setRecyclerGrid()
        adapter.notifyDataSetChanged()
    }

    private fun setRecyclerGrid() {
        val span: Int = when (mHosts.size) {
            0, 1 -> 1
            2, 3, 4 -> 2
            else -> 3
        }
        val gridLayoutManager = GridLayoutManager(activity, span)
        recyclerView.layoutManager = gridLayoutManager
    }

    private fun initialize() {
        recyclerView = rootView.findViewById(R.id.choose_recycler)
    }

    private fun initializeLogic() {
        setRecyclerGrid()
        adapter = ChooseHostRecyclerAdapter(activity!!, mHosts)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(view: View?, position: Int) {
        mListener!!.onChooseFragmentInteraction(mHosts.toTypedArray()[position])
    }

    interface OnFragmentInteractionListener {
        fun onChooseFragmentInteraction(host: Host)
    }

    companion object {
        private lateinit var mHosts: Set<Host>

        @JvmStatic
        fun newInstance(hosts: Set<Host>): ChooseDeviceFragment {
            mHosts = hosts
            return ChooseDeviceFragment()
        }
    }
}