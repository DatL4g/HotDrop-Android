package de.datlag.hotdrop.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adroitandroid.near.model.Host;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Set;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.datlag.hotdrop.R;

public class ChooseHostRecyclerAdapter extends RecyclerView.Adapter<ChooseHostRecyclerAdapter.ViewHolder> {

    private Activity activity;
    private Set<Host> nsdDevices;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public ChooseHostRecyclerAdapter(Activity activity, Set<Host> data) {
        this.activity = activity;
        this.mInflater = LayoutInflater.from(this.activity);
        this.nsdDevices = data;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.choose_host_recycler_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String hostName = ((Host) nsdDevices.toArray()[position]).getName();
        switch (hostName.charAt(0)) {
            case 1:
            case '1':
                holder.deviceFAB.setImageResource(R.drawable.ic_phone_android_white_24dp);
                break;
            case 2:
            case '2':
                holder.deviceFAB.setImageResource(R.drawable.ic_smartphone_white_24dp);
                break;
            case 3:
            case '3':
                holder.deviceFAB.setImageResource(R.drawable.ic_tablet_android_white_24dp);
                break;
            case 4:
            case '4':
                holder.deviceFAB.setImageResource(R.drawable.ic_tv_white_24dp);
                break;

            default:
                holder.deviceFAB.setImageResource(R.drawable.ic_watch_white_24dp);
                break;
        }
        String deviceUserName = hostName.substring(hostName.indexOf(activity.getPackageName()) + activity.getPackageName().length() +1);
        holder.deviceName.setText(deviceUserName);

        holder.deviceContainer.setOnFocusChangeListener((View view, boolean b) -> {
            if (b) {
                holder.deviceFAB.requestFocus();
            }
        });
    }


    @Override
    public int getItemCount() {
        return nsdDevices.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayoutCompat deviceContainer;
        FloatingActionButton deviceFAB;
        AppCompatTextView deviceName;

        ViewHolder(View itemView) {
            super(itemView);
            deviceContainer = itemView.findViewById(R.id.device_container);
            deviceFAB = itemView.findViewById(R.id.fab_device);
            deviceName = itemView.findViewById(R.id.device_name);
            itemView.setOnClickListener(this);
            deviceFAB.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    Host getItem(int position) {
        return (Host) nsdDevices.toArray()[position];
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
