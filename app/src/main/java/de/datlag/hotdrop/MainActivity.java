package de.datlag.hotdrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;

public class MainActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int LOCATION_PERMISSION_CODE = 420;
    private static final int STORAGE_READ_PERMISSION_CODE = 421;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 422;

    private Activity activity;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private AppCompatImageView tuneButton;
    private AppCompatImageView revealButton;
    private AppCompatImageView reverseRevealButton;
    private RevealFrameLayout revealLinearLayout;
    private FrameLayout infoLayout;
    private FrameLayout mainLayout;
    private AppCompatImageView githubIcon;
    private AppCompatImageView codeIcon;
    private AppCompatImageView helpIcon;

    private Markwon markwon;
    private String[] settingsArray;

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
        tuneButton = findViewById(R.id.tune_button);
        revealButton = findViewById(R.id.reveal_button);
        revealLinearLayout = findViewById(R.id.reveal_parent);
        infoLayout = findViewById(R.id.info_layout);
        mainLayout = findViewById(R.id.frame);
        reverseRevealButton = findViewById(R.id.reverse_reveal);
        githubIcon = findViewById(R.id.github_icon);
        codeIcon = findViewById(R.id.code_icon);
        helpIcon = findViewById(R.id.help_icon);
    }

    private void initializeLogic() {
        activity = MainActivity.this;
        settingsArray = getResources().getStringArray(R.array.available_settings);
        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();
        setSupportActionBar(toolbar);
        switch2Fragment(OpenHotDropFragment.newInstance("Test"));

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

        tuneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(getString(R.string.settings))
                        .setItems(settingsArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switchSettings(which);
                            }
                        })
                        .setPositiveButton(getString(R.string.close), null)
                        .show();
            }
        });

        githubIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_repo)));
                startActivity(browserIntent);
            }
        });

        codeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(getString(R.string.dependencies))
                        .setMessage("All libs...")
                        .setPositiveButton(getString(R.string.okay), null)
                        .show();
            }
        });

        final Spanned markdown = markwon.toMarkdown("**Hello there!**<br><a href=\"google.com\">Google</a>");


        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle("Info App / Creator")
                .setMessage("All information...")
                .setPositiveButton(getString(R.string.okay), null)
                .setNeutralButton(getString(R.string.data_protection_title), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(getString(R.string.data_protection_title))
                                .setMessage(markdown)
                                .setPositiveButton(getString(R.string.okay), null)
                                .setNeutralButton(getString(R.string.open_in_browser), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_repo)));
                                        startActivity(browserIntent);
                                    }
                                })
                                .show();
                    }
                })
                .show();
            }
        });
        
        permissionCheck(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new int[]{
                LOCATION_PERMISSION_CODE,
                STORAGE_READ_PERMISSION_CODE,
                STORAGE_WRITE_PERMISSION_CODE
        });
    }

    private void permissionCheck(@NotNull String[] permissions, final int[] permissionCodes) {
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            final int permissionCode = permissionCodes[i];

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

                switch (permission) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        builder.setTitle(getString(R.string.location));
                        builder.setMessage(getString(R.string.location_needed));

                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        builder.setTitle("Read Storage");
                        builder.setMessage("This permission is needed to send and receive files and folders");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        builder.setTitle("Write Storage");
                        builder.setMessage("This permission is needed to send and receive files and folders");
                        break;
                }

                builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCode);
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCodes[i]);
            }


        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch (requestCode) {
                case LOCATION_PERMISSION_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // enable P2P
                    }
                    break;
                case STORAGE_READ_PERMISSION_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // read storage
                    }
                    break;
                case STORAGE_WRITE_PERMISSION_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // write storage
                    }
                    break;
            }
    }

    private void switchSettings(int selected) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(settingsArray[selected]);

        switch (selected) {
            case 0:
                builder.setMessage("Test: selected "+selected);
                break;
            case 1:
                builder.setMessage("Test: selected "+selected);
                break;
            case 2:
                ArrayList<String> statusList = new ArrayList<>();

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Location: Granted");
                } else {
                    statusList.add("Location: Denied");
                }

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Read Storage: Granted");
                } else {
                    statusList.add("Read Storage: Denied");
                }

                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    statusList.add("Write Storage: Granted");
                } else {
                    statusList.add("Write Storage: Denied");
                }

                String[] status = statusList.toArray(new String[0]);

                builder.setItems(status, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                permissionCheck(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{LOCATION_PERMISSION_CODE});
                                break;
                            case 1:
                                permissionCheck(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new int[]{STORAGE_READ_PERMISSION_CODE});
                                break;
                            case 2:
                                permissionCheck(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{STORAGE_WRITE_PERMISSION_CODE});
                                break;
                        }
                    }
                });
                builder.setPositiveButton(getString(R.string.close), null);
                break;
        }

        builder.create().show();
    }
}
