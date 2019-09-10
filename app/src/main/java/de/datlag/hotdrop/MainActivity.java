package de.datlag.hotdrop;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.adroitandroid.near.model.Host;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.datlag.hotdrop.utils.DiscoverHost;
import de.datlag.hotdrop.utils.InfoPageManager;
import de.datlag.hotdrop.utils.PermissionManager;
import de.datlag.hotdrop.utils.SettingsManager;
import de.interaapps.firebasemanager.auth.AnonymousAuth;
import de.interaapps.firebasemanager.auth.GoogleAuth;
import de.interaapps.firebasemanager.auth.PlayGamesAuth;
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
    private FloatingActionButton fabUpload;
    private FloatingActionButton fabDownload;
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

    private SettingsManager settingsManager;
    private PermissionManager permissionManager;
    private FirebaseManager firebaseManager;
    private GoogleAuth googleAuth;
    private PlayGamesAuth playGamesAuth;
    private AnonymousAuth anonymousAuth;

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
        fabUpload = findViewById(R.id.fab_upload);
        fabDownload = findViewById(R.id.fab_download);
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

        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();
        setSupportActionBar(toolbar);

        discoverHost = new DiscoverHost(activity);
        searchDeviceFragment = SearchDeviceFragment.newInstance();
        switch2Fragment(searchDeviceFragment);
        permissionManager = new PermissionManager(activity);

        firebaseManager = new FirebaseManager(activity);
        googleAuth = new GoogleAuth(activity, getString(R.string.default_web_client_id));
        playGamesAuth = new PlayGamesAuth(activity, getString(R.string.default_web_client_id));
        anonymousAuth = new AnonymousAuth(activity);
        firebaseManager.addLogin(googleAuth);
        firebaseManager.addLogin(playGamesAuth);
        firebaseManager.addLogin(anonymousAuth);
        settingsManager = new SettingsManager(activity, firebaseManager, permissionManager, new SettingsManager.LoginCallbacks() {
            @Override
            public void onLoginSuccessful(AuthResult authResult) {
                String loginName = null;
                if (authResult.getUser() != null) {
                    loginName = (authResult.getUser().isAnonymous()) ? "Guest" : authResult.getUser().getDisplayName();
                }

                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.logged_in_as) + loginName, Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }

            @Override
            public void onLoginFailed() {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                showSnackbar(snackbar);
            }
        });

        infoPageManager = new InfoPageManager();
        infoPageManager.setLayouts(mainLayout, infoLayout, appBarLayout, toolbar, getSupportActionBar());

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

        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLoggedIn = false;
                for (Auth auth : firebaseManager.getLogin().toArray(new Auth[0])) {
                    isLoggedIn = auth.getUser() != null;
                }

                if (!isLoggedIn) {
                    new MaterialAlertDialogBuilder(activity)
                            .setTitle("Account")
                            .setMessage("You must be logged in to use this feature!\nIf you don't want to create an account you can sign in anonymously, but your files are less secure!")
                            .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    chooseFile(new FileChooseCallback() {
                                        @Override
                                        public void onChosen(String path, File file) {
                                            Log.e("Path", path);
                                            Log.e("File", file.getName());
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create().show();
                } else {
                    chooseFile(new FileChooseCallback() {
                        @Override
                        public void onChosen(String path, File file) {
                            Log.e("Path", path);
                            Log.e("File", file.getName());
                        }
                    });
                }
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

    public void chooseFile(FileChooseCallback fileChooseCallback) {
        new ChooserDialog(MainActivity.this, R.style.FileChooserStyle)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        fileChooseCallback.onChosen(path, pathFile);
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

    public interface FileChooseCallback {
        void onChosen(String path, File file);
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
        playGamesAuth.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
