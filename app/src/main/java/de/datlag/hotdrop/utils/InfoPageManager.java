package de.datlag.hotdrop.utils;

import android.animation.Animator;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import io.codetail.animation.ViewAnimationUtils;

public class InfoPageManager {
    private FrameLayout mainLayout;
    private FrameLayout infoLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private ActionBar actionBar;

    public void setLayouts(FrameLayout mainLayout, FrameLayout infoLayout, AppBarLayout appBarLayout, Toolbar toolbar, ActionBar actionBar) {
        this.mainLayout = mainLayout;
        this.infoLayout = infoLayout;
        this.appBarLayout = appBarLayout;
        this.toolbar = toolbar;
        this.actionBar = actionBar;
    }

    public void startAnimation(boolean isReverse) {
        infoReveal(infoLayout, mainLayout.getRight(), 0, 1000, isReverse);
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
        if (isReverse) {
            animator = ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, finalRadius, 0);
        } else {
            animator = ViewAnimationUtils.createCircularReveal(targetView, startPointX, startPointY, 0, finalRadius);
        }

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isReverse) {
                    mainLayout.setVisibility(View.VISIBLE);
                    targetView.setVisibility(View.GONE);
                } else {
                    mainLayout.setVisibility(View.GONE);
                    targetView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        if (isReverse) {
            targetView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appBarLayout.setElevation(8);
            } else {
                actionBar.setElevation(8);
            }
            mainLayout.setVisibility(View.VISIBLE);
        } else {
            targetView.setVisibility(View.VISIBLE);
            actionBar.setElevation(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appBarLayout.setElevation(0);
                toolbar.setElevation(0);
            }
        }
        animator.start();
    }
}