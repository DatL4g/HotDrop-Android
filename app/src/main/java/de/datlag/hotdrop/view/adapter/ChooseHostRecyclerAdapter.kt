package de.datlag.hotdrop.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.hotdrop.R
import de.datlag.hotdrop.p2p.Host
import de.datlag.hotdrop.util.DeviceUtil

class ChooseHostRecyclerAdapter(private val activity: Activity, data: Set<Host>) : RecyclerView.Adapter<ChooseHostRecyclerAdapter.ViewHolder>() {
    private val devices: Set<Host> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var mClickListener: ItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.choose_host_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device: Host = getItem(position)
        when (device.type) {
            DeviceUtil.DeviceType.WATCH -> holder.deviceFAB.setImageResource(R.drawable.ic_watch_white_24dp)
            DeviceUtil.DeviceType.PHONE -> holder.deviceFAB.setImageResource(R.drawable.ic_phone_android_white_24dp)
            DeviceUtil.DeviceType.PHABLET -> holder.deviceFAB.setImageResource(R.drawable.ic_smartphone_white_24dp)
            DeviceUtil.DeviceType.TABLET -> holder.deviceFAB.setImageResource(R.drawable.ic_tablet_android_white_24dp)
            DeviceUtil.DeviceType.TV -> holder.deviceFAB.setImageResource(R.drawable.ic_tv_white_24dp)
        }
        holder.deviceName.text = device.name
        holder.deviceContainer.onFocusChangeListener = OnFocusChangeListener { _: View?, b: Boolean ->
            if (b) {
                holder.deviceFAB.requestFocus()
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var deviceContainer: LinearLayoutCompat = itemView.findViewById(R.id.device_container)
        var deviceFAB: FloatingActionButton = itemView.findViewById(R.id.fab_device)
        var deviceName: AppCompatTextView = itemView.findViewById(R.id.device_name)

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
            deviceFAB.setOnClickListener(this)
        }
    }

    fun getItem(position: Int): Host {
        return devices.toTypedArray()[position]
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

}