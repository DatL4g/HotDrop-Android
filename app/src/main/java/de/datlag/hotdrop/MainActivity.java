package de.datlag.hotdrop;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adroitandroid.near.model.Host;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.datlag.hotdrop.firebase.StorageManager;
import de.datlag.hotdrop.utils.DiscoverHost;
import de.datlag.hotdrop.utils.FileUtil;
import de.datlag.hotdrop.utils.InfoPageManager;
import de.datlag.hotdrop.utils.SettingsManager;
import de.interaapps.firebasemanager.auth.AnonymousAuth;
import de.interaapps.firebasemanager.auth.EmailAuth;
import de.interaapps.firebasemanager.auth.GoogleAuth;
import de.interaapps.firebasemanager.auth.OAuth;
import de.interaapps.firebasemanager.auth.OAuthEnum;
import de.interaapps.firebasemanager.core.FirebaseManager;
import de.interaapps.firebasemanager.core.auth.Auth;
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
    private SpeedDialView speedDialView;
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
    private InfoPageManager infoPageManager;

    private Markwon markwon;
    private static final int downloadID = ViewCompat.generateViewId();
    private static final int uploadID = ViewCompat.generateViewId();
    private ArrayList<SpeedDialActionItem> menuItems = new ArrayList<>();

    private SettingsManager settingsManager;
    private FirebaseManager firebaseManager;
    private GoogleAuth googleAuth;
    private EmailAuth emailAuth;
    private OAuth githubAuth;
    private AnonymousAuth anonymousAuth;
    private StorageManager storageManager;

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
        activity = MainActivity.this;

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                String message = "";
                                String[] permissionInfo = getResources().getStringArray(R.array.permission_info);
                                String[] permissionNotGranted = getResources().getStringArray(R.array.permission_not_granted);

                                for (int i = 0; i < permissionNotGranted.length; i++) {
                                    message += permissionNotGranted[i].concat((i+1 == permissionNotGranted.length) ? "\n\n\n" : "\n");
                                }

                                for (int i = 0; i < permissionInfo.length; i++) {
                                    message += permissionInfo[i].concat((i+1 == permissionInfo.length) ? "" : "\n\n");
                                }


                                new MaterialAlertDialogBuilder(activity)
                                        .setTitle(getString(R.string.not_granted))
                                        .setMessage(message)
                                        .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finishAffinity();
                                            }
                                        })
                                        .create().show();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        String message = "";
                        String[] permissionInfo = getResources().getStringArray(R.array.permission_info);
                        for (int i = 0; i < permissionInfo.length; i++) {
                            message += permissionInfo[i].concat((i+1 == permissionInfo.length) ? "" : "\n\n");
                        }

                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(getString(R.string.location_storage))
                                .setMessage(message)
                                .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        token.continuePermissionRequest();
                                    }
                                })
                                .create().show();
                    }
                })
                .check();

        initialize();
        initializeLogic();
    }

    private void initialize() {
        coordinatorLayout = findViewById(R.id.coordinator);
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        speedDialView = findViewById(R.id.speedDial);
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
        Glide.with(activity)
                .load(ContextCompat.getDrawable(activity, R.drawable.circles))
                .centerCrop()
                .apply(new RequestOptions().fitCenter())
                .override(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels)
                .into(backgroundImage);

        menuItems.add(new SpeedDialActionItem.Builder(downloadID, R.drawable.ic_cloud_download_white_24dp)
                .setFabImageTintColor(Color.WHITE)
                .create());
        menuItems.add(new SpeedDialActionItem.Builder(uploadID, R.drawable.ic_cloud_upload_white_24dp)
                .setFabImageTintColor(Color.WHITE)
                .create());
        speedDialView.addAllActionItems(menuItems);

        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();
        setSupportActionBar(toolbar);

        discoverHost = new DiscoverHost(activity);
        searchDeviceFragment = SearchDeviceFragment.newInstance();
        switch2Fragment(searchDeviceFragment);

        firebaseManager = new FirebaseManager(activity);
        googleAuth = new GoogleAuth(activity, getString(R.string.default_web_client_id));
        emailAuth = new EmailAuth(activity);
        githubAuth = new OAuth(activity, OAuthEnum.GITHUB);
        anonymousAuth = new AnonymousAuth(activity);
        firebaseManager.addLogin(googleAuth);
        firebaseManager.addLogin(emailAuth);
        firebaseManager.addLogin(githubAuth);
        firebaseManager.addLogin(anonymousAuth);
        settingsManager = new SettingsManager(activity, firebaseManager);
        storageManager = new StorageManager(activity, firebaseManager);

        infoPageManager = new InfoPageManager();
        infoPageManager.setLayouts(mainLayout, infoLayout, appBarLayout, getSupportActionBar());

        revealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPageManager.startAnimation(false);
            }
        });

        reverseRevealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoPageManager.startAnimation(true);
            }
        });

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                if (actionItem.getId() == uploadID) {
                    uploadCloud();
                } else if (actionItem.getId() == downloadID) {
                    //download
                }

                return false;
            }
        });

        tuneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsManager.open();
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


        final String mURL = getString(R.string.dsgvo_url)+"?viaJS=true";
        RequestQueue queue = Volley.newRequestQueue(activity);

        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar.make(((MainActivity) activity).getCoordinatorLayout(), "Loading...", Snackbar.LENGTH_LONG);
                ((MainActivity) activity).showSnackbar(snackbar);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                final Spanned markdown = markwon.toMarkdown(response);
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
                                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dsgvo_url)));
                                                                startActivity(browserIntent);
                                                            }
                                                        })
                                                        .show();
                                            }
                                        })
                                        .show();
                            }
                            }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

                queue.add(stringRequest);
            }
        });

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

    private void uploadCloud() {
        FirebaseUser firebaseUser = getUser();

        if (firebaseUser == null) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(getString(R.string.account))
                    .setMessage(getString(R.string.upload_info))
                    .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            settingsManager.switchSettings(0);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create().show();
        } else {
            storageManager.startUploadFile(firebaseUser.isAnonymous(), new StorageManager.FileUploadCallback() {
                @Override
                public void onSuccess(String downbloadUri) {
                    if (firebaseUser.isAnonymous()) {
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle("File uploaded")
                                .setMessage("Please share this link, otherwise you cannot Download this file or have any access to it")
                                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Get this file from HotDrop!");
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, downbloadUri);
                                        sendIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(sendIntent, "Share URL"));
                                    }
                                })
                                .setNeutralButton("Copy Link", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("DownloadUrl", downbloadUri);
                                        clipboard.setPrimaryClip(clip);
                                    }
                                })
                                .create().show();
                    } else {
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "File uploaded!", Snackbar.LENGTH_LONG);
                        showSnackbar(snackbar);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Upload failed!", Snackbar.LENGTH_LONG);
                    showSnackbar(snackbar);
                }
            });
        }
    }

    public void showSnackbar(@NotNull Snackbar snackbar) {
        final View snackBarView = snackbar.getView();
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
        final TextView snackBarText = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            snackBarText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            snackBarText.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        snackBarText.setTypeface(snackBarText.getTypeface(), Typeface.BOLD);

        params.setMargins(params.leftMargin + (int) activity.getResources().getDimension(R.dimen.snackbar_margin),
                params.topMargin,
                params.rightMargin + (int) activity.getResources().getDimension(R.dimen.snackbar_margin),
                params.bottomMargin + (int) activity.getResources().getDimension(R.dimen.snackbar_margin));

        snackBarView.setLayoutParams(params);
        snackbar.show();
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    private FirebaseUser getUser() {
        for (Auth auth : firebaseManager.getLogin()) {
            if (auth.getUser() != null) {
                return auth.getUser();
            }
        }

        return FirebaseAuth.getInstance().getCurrentUser();
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
            firebaseManager.onStart();
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
        activity.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleAuth.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
