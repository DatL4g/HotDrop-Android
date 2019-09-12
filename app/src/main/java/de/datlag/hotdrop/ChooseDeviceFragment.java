package de.datlag.hotdrop;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adroitandroid.near.model.Host;

import org.jetbrains.annotations.NotNull;

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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

    public void setHosts(Set<Host> hosts) {
        this.mHosts = hosts;
        setRecyclerGrid();
        adapter.notifyDataSetChanged();
    }

    private void setRecyclerGrid() {
        int span;
        switch (mHosts.size()) {
            case 1:
                span = 1;
                break;
            case 2:
            case 3:
            case 4:
                span = 2;
                break;

            default:
                span = 3;
                break;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), span);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void initialize() {
        recyclerView = rootView.findViewById(R.id.choose_recycler);
    }

    private void initializeLogic() {
        setRecyclerGrid();
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
