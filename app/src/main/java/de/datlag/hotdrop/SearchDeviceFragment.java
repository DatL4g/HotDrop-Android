package de.datlag.hotdrop;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.utils.CircularAnimation;

public class SearchDeviceFragment extends Fragment {

    private View rootView;
    private FloatingActionButton searchFAB;
    private boolean search = false;
    private CircularAnimation rotateAnimation;

    private OnFragmentInteractionListener mListener;

    public SearchDeviceFragment() {}

    public static SearchDeviceFragment newInstance() {
        SearchDeviceFragment fragment = new SearchDeviceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_device, container, false);
        initialize();
        initializeLogic();
        return rootView;
    }

    public void setSearch(boolean search) {
        this.search = search;

        if (this.search) {
            rotateAnimation = new CircularAnimation(searchFAB, 25);
            rotateAnimation.setDuration(10000);
            rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            rotateAnimation.setRepeatMode(Animation.RESTART);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            searchFAB.startAnimation(rotateAnimation);
        } else {
            rotateAnimation.cancel();
            rotateAnimation.reset();
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
        searchFAB = rootView.findViewById(R.id.fab_search);
    }

    private void initializeLogic() {
        searchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    setSearch(!search);
                    mListener.onSearchFragmentInteraction(search);
                }
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onSearchFragmentInteraction(boolean search);
    }
}
