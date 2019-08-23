package de.datlag.hotdrop;

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
import android.os.Looper;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adroitandroid.near.connect.NearConnect;
import com.adroitandroid.near.discovery.NearDiscovery;
import com.adroitandroid.near.model.Host;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PlayGamesAuthProvider;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;
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

    private static final int LOCATION_PERMISSION_CODE = 420;
    private static final int STORAGE_READ_PERMISSION_CODE = 421;
    private static final int STORAGE_WRITE_PERMISSION_CODE = 422;

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

    private static final int RC_SIGN_IN = 520;
    private static final int GAMES_SIGN_IN = 521;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private GoogleSignInClient mGoogleSignInClient;

    private NearDiscovery mNearDiscovery;
    private NearConnect mNearConnect;
    public static final String MESSAGE_REQUEST_START_TRANSFER = "start_chat";
    public static final String MESSAGE_RESPONSE_DECLINE_REQUEST = "decline_request";
    public static final String MESSAGE_RESPONSE_ACCEPT_REQUEST = "accept_request";

    private SearchDeviceFragment searchDeviceFragment;
    private ChooseDeviceFragment chooseDeviceFragment = null;

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

        mNearDiscovery = new NearDiscovery.Builder()
                .setContext(this)
                .setDiscoverableTimeoutMillis(Long.MAX_VALUE)
                .setDiscoveryTimeoutMillis(Long.MAX_VALUE)
                .setDiscoverablePingIntervalMillis(500)
                .setDiscoveryListener(getNearDiscoveryListener(), Looper.getMainLooper())
                .build();

        mNearConnect = new NearConnect.Builder()
                .fromDiscovery(mNearDiscovery)
                .setContext(this)
                .setListener(getNearConnectListener(), Looper.getMainLooper())
                .build();


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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInOptions gsoGames = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        mAuth = FirebaseAuth.getInstance();

        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();
        setSupportActionBar(toolbar);
        searchDeviceFragment = SearchDeviceFragment.newInstance();
        switch2Fragment(searchDeviceFragment);

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
                if (mUser != null) {
                    builder.setTitle("Logged in");
                    String userName = (mUser.getDisplayName() != null) ? mUser.getDisplayName() : "Guest";
                    builder.setMessage("Welcome, " + userName);
                    builder.setPositiveButton(getString(R.string.close), null);
                    builder.setNeutralButton("Log out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signOut();
                        }
                    });
                    break;
                }
                builder.setItems(signInArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signIn(which);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        switch (requestCode) {
            case RC_SIGN_IN:
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException ignored) {}
                break;

            case GAMES_SIGN_IN:
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithPlayGames(account);
                } catch (ApiException ignored) {}
                break;
        }
    }

    private void firebaseAuthWithGoogle(@NotNull GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.logged_in_as) + mUser.getDisplayName(), Snackbar.LENGTH_LONG);
                            showSnackbar(snackbar);
                        } else {
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                            showSnackbar(snackbar);
                        }
                    }
                });
    }

    private void firebaseAuthWithPlayGames(@NotNull GoogleSignInAccount acct) {
        AuthCredential credential = PlayGamesAuthProvider.getCredential(acct.getServerAuthCode());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUser = mAuth.getCurrentUser();
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.logged_in_as) + mUser.getDisplayName(), Snackbar.LENGTH_LONG);
                            showSnackbar(snackbar);
                        } else {
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                            showSnackbar(snackbar);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUser = mAuth.getCurrentUser();
    }

    private void signIn(int selected) {
        switch (selected) {
            case 0:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;

            case 1:
                Intent signInGames = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInGames, GAMES_SIGN_IN);
                break;

            case 2:
                mAuth.signInAnonymously()
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mUser = mAuth.getCurrentUser();
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.sign_in_guest), Snackbar.LENGTH_LONG);
                                    showSnackbar(snackbar);
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.login_failed), Snackbar.LENGTH_LONG);
                                    showSnackbar(snackbar);
                                }
                            }
                        });
                break;
        }
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.sing_out), Snackbar.LENGTH_LONG);
                            showSnackbar(snackbar);
                        }
                        if (mUser.isAnonymous()) {
                            mUser.delete();
                        }
                        mUser = null;
                    }
                });
    }

    public void showSnackbar(@NotNull Snackbar snackbar) {
        final View snackBarView = snackbar.getView();
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackBarView.getLayoutParams();
        final TextView snackBarText = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);

        snackBarText.setGravity(Gravity.CENTER);
        snackBarText.setTypeface(snackBarText.getTypeface(), Typeface.BOLD);

        params.setMargins(params.leftMargin + (int) getResources().getDimension(R.dimen.snackbar_margin),
                params.topMargin,
                params.rightMargin + (int) getResources().getDimension(R.dimen.snackbar_margin),
                params.bottomMargin + (int) getResources().getDimension(R.dimen.snackbar_margin));

        snackBarView.setLayoutParams(params);
        snackbar.show();
    }

    @Contract(value = " -> new", pure = true)
    @NonNull
    private NearDiscovery.Listener getNearDiscoveryListener() {
        return new NearDiscovery.Listener() {
            @Override
            public void onPeersUpdate(Set<Host> hosts) {
                for (Host host : hosts) {
                    if (!host.getName().contains(getPackageName())) {
                        hosts.remove(host);
                    }
                }

                if (hosts.size() > 0) {
                    if (chooseDeviceFragment == null) {
                        chooseDeviceFragment = new ChooseDeviceFragment(hosts);
                    } else {
                        chooseDeviceFragment.setHosts(hosts);
                    }
                    switch2Fragment(chooseDeviceFragment);
                } else {
                    switch2Fragment(searchDeviceFragment);
                    if (mNearDiscovery.isDiscovering()) {
                        searchDeviceFragment.setSearch(true);
                    }
                }
            }

            @Override
            public void onDiscoveryTimeout() {
                stopDiscovery();
                searchDeviceFragment.setSearch(false);
            }

            @Override
            public void onDiscoveryFailure(Throwable e) {
                stopDiscovery();
                searchDeviceFragment.setSearch(false);
            }

            @Override
            public void onDiscoverableTimeout() {
                stopDiscovery();
                searchDeviceFragment.setSearch(false);
            }
        };
    }

    @Contract(value = " -> new", pure = true)
    @NonNull
    private NearConnect.Listener getNearConnectListener() {
        return new NearConnect.Listener() {
            @Override
            public void onReceive(byte[] bytes, final Host sender) {
                if (bytes != null) {
                    switch (new String(bytes)) {
                        case MESSAGE_REQUEST_START_TRANSFER:
                            new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setMessage(sender.getName().substring(sender.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length() +1)
                                            + " would like to start chatting with you.")
                                    .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mNearConnect.send(MESSAGE_RESPONSE_ACCEPT_REQUEST.getBytes(), sender);
                                            Log.e("Chatting", "start");
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mNearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.getBytes(), sender);
                                        }
                                    }).create().show();
                            break;
                        case MESSAGE_RESPONSE_DECLINE_REQUEST:
                            new MaterialAlertDialogBuilder(MainActivity.this)
                                    .setMessage(sender.getName().substring(sender.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length() +1)
                                            + " is busy at the moment.")
                                    .setNeutralButton("Ok", null).create().show();
                            break;
                        case MESSAGE_RESPONSE_ACCEPT_REQUEST:
                            Log.e("Chatting", "start");
                            break;
                    }
                }
            }

            @Override
            public void onSendComplete(long jobId) {
                // jobId is the same as the return value of NearConnect.send(), an approximate epoch time of the send
            }

            @Override
            public void onSendFailure(Throwable e, long jobId) {
                // handle failed sends here
            }

            @Override
            public void onStartListenFailure(Throwable e) {
                // This tells that the NearConnect.startReceiving() didn't go through properly.
                // Common cause would be that another instance of NearConnect is already listening and it's NearConnect.stopReceiving() needs to be called first
            }
        };
    }

    private void startDiscovery() {
        EasyDeviceMod easyDeviceMod = new EasyDeviceMod(activity);
        if (!mNearDiscovery.isDiscovering()) {
            mNearDiscovery.makeDiscoverable(easyDeviceMod.getDeviceType(activity) + getPackageName() + "_" + Build.MODEL);
            if (!mNearConnect.isReceiving()) {
                mNearConnect.startReceiving();
            }

            mNearDiscovery.startDiscovery();
        }
    }

    private void stopDiscovery() {
        if (mNearDiscovery.isDiscovering()) {
            mNearDiscovery.makeNonDiscoverable();
            mNearDiscovery.stopDiscovery();
        }
        if (mNearConnect.isReceiving()) {
            mNearConnect.stopReceiving(false);
            searchDeviceFragment.setSearch(false);
        }
    }

    @Override
    public void onSearchFragmentInteraction(boolean search) {
        if (search) {
            startDiscovery();
        } else {
            stopDiscovery();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDiscovery();
    }

    @Override
    public void onChooseFragmentInteraction(Host host) {
        mNearConnect.send(MESSAGE_REQUEST_START_TRANSFER.getBytes(), host);
    }
}
