package de.datlag.hotdrop.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.p2p.DiscoverHost;
import de.datlag.hotdrop.view.animation.CircularAnimation;

public class SearchDeviceFragment extends Fragment {

    private static AdvancedActivity activity;
    private View rootView;
    private FloatingActionButton searchFAB;
    private boolean search = false;
    private CircularAnimation rotateAnimation;
    private static DiscoverHost discoverHost;

    public static SearchDeviceFragment newInstance(AdvancedActivity activity, DiscoverHost discoverHost) {
        SearchDeviceFragment.activity = activity;
        SearchDeviceFragment.discoverHost = discoverHost;
        return new SearchDeviceFragment();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            discoverHost.startDiscovery();
        } else {
            rotateAnimation.cancel();
            rotateAnimation.reset();
            searchFAB.clearAnimation();
            discoverHost.stopDiscovery();
        }
    }

    private void initialize() {
        searchFAB = rootView.findViewById(R.id.fab_search);
    }

    private void initializeLogic() {
        searchFAB.setOnClickListener((View view) -> {
            setSearch(!search);
        });
    }
}
