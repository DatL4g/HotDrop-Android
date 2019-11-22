package de.datlag.hotdrop.manager;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Spanned;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import io.codetail.animation.ViewAnimationUtils;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import lombok.Setter;

public class InfoPageManager {

    @Setter
    private AdvancedActivity activity;

    @Setter
    private FrameLayout mainLayout;

    @Setter
    private FrameLayout infoLayout;

    @Setter
    private AppCompatImageView githubIcon;

    @Setter
    private AppCompatImageView codeIcon;

    @Setter
    private AppCompatImageView helpIcon;
    private static final long duration = 1000;

    public InfoPageManager(AdvancedActivity activity, FrameLayout mainLayout, FrameLayout infoLayout) {
        this.activity = activity;
        this.mainLayout = mainLayout;
        this.infoLayout = infoLayout;
    }

    public void init() {
        githubIcon.setOnClickListener((View v) -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.github_repo)));
            activity.startActivity(browserIntent);
        });

        codeIcon.setOnClickListener((View v) -> {
            activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.dependencies))
                    .setItems(activity.getResources().getStringArray(R.array.dependencies), null)
                    .setPositiveButton(activity.getString(R.string.okay), null)
                    .create())
                    .show();
        });


        helpIcon.setOnClickListener(v -> {
            informationDialog();
        });
    }

    public void start(boolean isReverse) {
        if (isReverse) {
            infoLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
        } else {
            infoReveal();
        }
    }

    private void infoReveal() {
        int dx = Math.max(mainLayout.getRight(), infoLayout.getWidth() - mainLayout.getRight());
        int dy = Math.max(0, infoLayout.getHeight());
        float finalRadius = (float) Math.hypot(dx, dy);
        Animator animator = ViewAnimationUtils.createCircularReveal(infoLayout, mainLayout.getRight(), 0, 0, finalRadius);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mainLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mainLayout.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        infoLayout.setVisibility(View.VISIBLE);
        animator.start();
    }

    public void informationDialog(){

        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle("Info App / Creator")
                .setMessage("All information...")
                .setPositiveButton(activity.getString(R.string.okay), null)
                .setNeutralButton(activity.getString(R.string.data_protection_title), (dialogInterface, i) -> privacyPolicies())
                .create()).show();
    }

    public void privacyPolicies(){
        final String mURL = activity.getString(R.string.dsgvo_url)+"?viaJS=true";
        RequestQueue queue = Volley.newRequestQueue(activity);

        Markwon markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                response -> {
                    final Spanned markdown = markwon.toMarkdown(response);

                    activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                            .setTitle(activity.getString(R.string.data_protection_title))
                            .setMessage(markdown)
                            .setPositiveButton(activity.getString(R.string.okay), null)
                            .setNeutralButton(activity.getString(R.string.open_in_browser), (dialog, which) -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.dsgvo_url)));
                                activity.startActivity(browserIntent);
                            })
                            .create()).show();
                }, error -> {

        });
        queue.add(stringRequest);
    }
}
