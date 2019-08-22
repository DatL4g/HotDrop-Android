package de.datlag.hotdrop;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adroitandroid.near.model.Host;
import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Set;

import de.datlag.hotdrop.utils.ChooseHostRecyclerAdapter;

public class ChooseDeviceFragment extends Fragment implements ChooseHostRecyclerAdapter.ItemClickListener {
    private Set<Host> mHosts;
    private View rootView;
    private ChooseHostRecyclerAdapter adapter;
    private RecyclerView recyclerView;

    private OnFragmentInteractionListener mListener;

    public ChooseDeviceFragment(Set<Host> hosts) {
        mHosts = hosts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_choose_device, container, false);
        initialize();
        initializeLogic();
        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onChooseFragmentInteraction((Host) mHosts.toArray()[0]);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initialize() {
        recyclerView = rootView.findViewById(R.id.choose_recycler);
    }

    private void initializeLogic() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
        layoutManager.setFlexDirection(FlexDirection.COLUMN);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChooseHostRecyclerAdapter(getActivity(), mHosts);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.e("Item", ((Host) mHosts.toArray()[position]).getName());
        if (mListener != null) {
            mListener.onChooseFragmentInteraction((Host) mHosts.toArray()[position]);
        }
    }

    public interface OnFragmentInteractionListener {
        void onChooseFragmentInteraction(Host host);
    }
}
