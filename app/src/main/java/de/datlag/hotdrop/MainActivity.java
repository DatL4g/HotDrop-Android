package de.datlag.hotdrop;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adroitandroid.near.model.Host;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import de.datlag.hotdrop.utils.DiscoverHost;
import de.datlag.hotdrop.utils.FirebaseManager;
import de.datlag.hotdrop.utils.PermissionManager;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;

public class MainActivity extends AppCompatActivity implements SearchDeviceFragment.OnFragmentInteractionListener, ChooseDeviceFragment.OnFragmentInteractionListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private Activity activity;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private AppCompatImageView backgroundImage;
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
    private String[] signInArray;

    private PermissionManager permissionManager;
    private FirebaseManager firebaseManager;

    private DiscoverHost discoverHost;
    public SearchDeviceFragment searchDeviceFragment;

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
        coordinatorLayout = findViewById(R.id.coordinator);
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        backgroundImage = findViewById(R.id.background_image);
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
        Glide.with(activity).load(ContextCompat.getDrawable(activity, R.drawable.circles)).centerCrop().into(backgroundImage);
        settingsArray = getResources().getStringArray(R.array.available_settings);
        signInArray = getResources().getStringArray(R.array.sign_in_options);

        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();
        setSupportActionBar(toolbar);
        discoverHost = new DiscoverHost(activity);
        searchDeviceFragment = SearchDeviceFragment.newInstance();
        switch2Fragment(searchDeviceFragment);
        permissionManager = new PermissionManager(activity);
        firebaseManager = new FirebaseManager(activity, new FirebaseManager.Callbacks() {

            @Override
            public void onGoogleLoginSuccessful(GoogleSignInAccount account) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.logged_in_as) + firebaseManager.getUser().getDisplayName(), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onGoogleLoginFailed() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onPlayGamesLoginSuccessful(GoogleSignInAccount account) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.logged_in_as) + firebaseManager.getUser().getDisplayName(), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onPlayGamesLoginFailed() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onAnonymouslyLoginSuccessful() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.sign_in_guest), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onAnonymouslyLoginFailed() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onSignOut() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.sing_out), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }
        });

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
                new ChooserDialog(MainActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                Log.e("Path", path);
                                Log.e("File", pathFile.getName());
                            }
                        })
                        // to handle the back key pressed or clicked outside the dialog:
                        .withOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                dialog.cancel();
                            }
                        })
                        .build()
                        .show();
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

        permissionManager.permissionCheck(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, new int[]{
                PermissionManager.LOCATION_PERMISSION_CODE,
                PermissionManager.STORAGE_READ_PERMISSION_CODE,
                PermissionManager.STORAGE_WRITE_PERMISSION_CODE
        });
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
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isReverse) {
                    mainLayout.setVisibility(View.GONE);
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

    private void switchSettings(int selected) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(settingsArray[selected]);

        switch (selected) {
            case 0:
                if (firebaseManager.getUser() != null) {
                    builder.setTitle("Logged in");
                    String userName = (firebaseManager.getUser().getDisplayName() != null) ? firebaseManager.getUser().getDisplayName() : "Guest";
                    builder.setMessage("Welcome, " + userName);
                    builder.setPositiveButton(getString(R.string.close), null);
                    builder.setNeutralButton("Log out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseManager.signOut();
                        }
                    });
                    break;
                }
                builder.setItems(signInArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseManager.signIn(selected);
                    }
                });
                builder.setPositiveButton(getString(R.string.close), null);
                break;
            case 1:
                builder.setMessage("Test: selected " + selected);
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
                                permissionManager.permissionCheck(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, new int[]{PermissionManager.LOCATION_PERMISSION_CODE});
                                break;
                            case 1:
                                permissionManager.permissionCheck(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new int[]{PermissionManager.STORAGE_READ_PERMISSION_CODE});
                                break;
                            case 2:
                                permissionManager.permissionCheck(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new int[]{PermissionManager.STORAGE_WRITE_PERMISSION_CODE});
                                break;
                        }
                    }
                });
                builder.setPositiveButton(getString(R.string.close), null);
                break;
        }

        builder.create().show();
    }

    public void showSnackbar(@NotNull Snackbar snackbar) {
        final View snackBarView = snackbar.getView();
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
        final TextView snackBarText = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            snackBarText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            snackBarText.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        snackBarText.setTypeface(snackBarText.getTypeface(), Typeface.BOLD);

        params.setMargins(params.leftMargin + (int) getResources().getDimension(R.dimen.snackbar_margin),
                params.topMargin,
                params.rightMargin + (int) getResources().getDimension(R.dimen.snackbar_margin),
                params.bottomMargin + (int) getResources().getDimension(R.dimen.snackbar_margin));

        snackBarView.setLayoutParams(params);
        snackbar.show();
    }

    public void setSearching(boolean searching) {
        searchDeviceFragment.setSearch(searching);
    }

    public void switch2Fragment(Fragment fragment) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment_view, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onSearchFragmentInteraction(boolean search) {
        if (search) {
            discoverHost.startDiscovery();
        } else {
            discoverHost.stopDiscovery();
        }
    }

    @Override
    public void onChooseFragmentInteraction(Host host) {
        discoverHost.send(host, DiscoverHost.MESSAGE_REQUEST_START_TRANSFER.getBytes());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseManager != null) {
            firebaseManager.setUser(firebaseManager.getAuth().getCurrentUser());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        discoverHost.stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        discoverHost.stopDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        firebaseManager.onActivityResult(requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
