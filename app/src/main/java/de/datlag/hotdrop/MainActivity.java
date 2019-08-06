package de.datlag.hotdrop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private AppCompatImageView revealButton;
    private AppCompatImageView reverseRevealButton;
    private RevealFrameLayout revealLinearLayout;
    private FrameLayout infoLayout;
    private FrameLayout mainLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        initializeLogic();
    }

    private void initialize() {
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        revealButton = findViewById(R.id.reveal_button);
        revealLinearLayout = findViewById(R.id.reveal_parent);
        infoLayout = findViewById(R.id.info_layout);
        mainLayout = findViewById(R.id.frame);
        reverseRevealButton = findViewById(R.id.reverse_reveal);
    }

    private void initializeLogic() {
        setSupportActionBar(toolbar);
        switch2Fragment(OpenSwapDropFragment.newInstance("Test"));

        revealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoReveal(infoLayout, mainLayout.getRight(), 0, 1000, false);
            }
        });

        reverseRevealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoReveal(infoLayout, mainLayout.getRight(), 0, 1000, true);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void switch2Fragment(Fragment fragment) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_view, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void infoReveal(@NotNull View targetView, int startPointX, int startPointY, long duration, final boolean isReverse) {
        if (isReverse) {
            targetView.setVisibility(View.VISIBLE);
            mainLayout.setVisibility(View.GONE);
        } else {
            targetView.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        }

        int dx = Math.max(startPointX, targetView.getWidth() - startPointX);
        int dy = Math.max(startPointY, targetView.getHeight() - startPointY);
        float finalRadius = (float) Math.hypot(dx, dy);
        Animator animator;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isReverse) {
                animator = android.view.ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, finalRadius, 0);
            } else {
                animator = android.view.ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, 0, finalRadius);
            }
        } else {
            if (isReverse) {
                animator = ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, finalRadius, 0);
            } else {
                animator = ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, 0, finalRadius);
            }
        }

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isReverse) {
                    mainLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        if (isReverse) {
            targetView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appBarLayout.setElevation(8);
            } else {
                getSupportActionBar().setElevation(8);
            }
            mainLayout.setVisibility(View.VISIBLE);
        } else {
            targetView.setVisibility(View.VISIBLE);
            getSupportActionBar().setElevation(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appBarLayout.setElevation(0);
                toolbar.setElevation(0);
            }
        }
        animator.start();
    }
}
