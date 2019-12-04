package de.datlag.hotdrop;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.adroitandroid.near.model.Host;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.firebase.StorageManager;
import de.datlag.hotdrop.fragment.ChooseDeviceFragment;
import de.datlag.hotdrop.fragment.SearchDeviceFragment;
import de.datlag.hotdrop.manager.InfoPageManager;
import de.datlag.hotdrop.manager.PermissionManager;
import de.datlag.hotdrop.manager.SettingsManager;
import de.datlag.hotdrop.p2p.DiscoverHost;
import de.interaapps.firebasemanager.auth.AnonymousAuth;
import de.interaapps.firebasemanager.auth.EmailAuth;
import de.interaapps.firebasemanager.auth.GoogleAuth;
import de.interaapps.firebasemanager.auth.OAuth;
import de.interaapps.firebasemanager.auth.OAuthEnum;
import de.interaapps.firebasemanager.core.FirebaseManager;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import lombok.Getter;

public class MainActivity extends AdvancedActivity implements ChooseDeviceFragment.OnFragmentInteractionListener {

    private AdvancedActivity activity;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private SpeedDialView speedDialView;
    private AppCompatImageView backgroundImage;
    private AppCompatImageView tuneButton;
    private AppCompatImageView revealButton;
    private AppCompatImageView reverseRevealButton;
    private FrameLayout infoLayout;
    private FrameLayout mainLayout;
    private AppCompatImageView githubIcon;
    private AppCompatImageView codeIcon;
    private AppCompatImageView helpIcon;
    private PermissionManager permissionManager;
    private InfoPageManager infoPageManager;
    private SettingsManager settingsManager;
    private FirebaseManager firebaseManager;
    private StorageManager storageManager;
    private AdView adView;
    private AdRequest adRequest;
    private GoogleAuth googleAuth;
    private EmailAuth emailAuth;
    private OAuth githubAuth;
    private AnonymousAuth anonymousAuth;
    private Markwon markwon;
    @Getter
    private SearchDeviceFragment searchDeviceFragment;
    private static final int downloadID = ViewCompat.generateViewId();
    private static final int uploadID = ViewCompat.generateViewId();
    private ArrayList<SpeedDialActionItem> menuItems = new ArrayList<>();
    private boolean adLoaded = false;
    private boolean speedDialMoved = false;
    private DiscoverHost discoverHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;

        initImportant();
        initViews();
        initViewLogic();
        initLogic();
        initListener();
        initDeeplink();
        initReceive();
    }

    private void initImportant() {
        createShortcuts();
        permissionManager = new PermissionManager(activity);
        permissionManager.check();

        MobileAds.initialize(activity, getString(R.string.admob_app_id));
        adRequest = new AdRequest.Builder().build();

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
        storageManager = new StorageManager(activity, firebaseManager, settingsManager);

        discoverHost = new DiscoverHost(activity);
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinator);
        toolbar = findViewById(R.id.toolbar);
        speedDialView = findViewById(R.id.speedDial);
        backgroundImage = findViewById(R.id.background_image);
        tuneButton = findViewById(R.id.tune_button);
        revealButton = findViewById(R.id.reveal_button);
        infoLayout = findViewById(R.id.info_layout);
        mainLayout = findViewById(R.id.frame);
        reverseRevealButton = findViewById(R.id.reverse_reveal);
        githubIcon = findViewById(R.id.github_icon);
        codeIcon = findViewById(R.id.code_icon);
        helpIcon = findViewById(R.id.help_icon);
        adView = findViewById(R.id.adView);
    }

    private void initViewLogic() {
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

        adView.loadAd(adRequest);
    }

    private void initLogic() {
        setSupportActionBar(toolbar);
        checkConfiguration();
        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();

        searchDeviceFragment = SearchDeviceFragment.newInstance(activity, discoverHost);
        switchFragment(searchDeviceFragment, R.id.fragment_view);

        infoPageManager = new InfoPageManager(activity, mainLayout, infoLayout);
        infoPageManager.setCodeIcon(codeIcon);
        infoPageManager.setGithubIcon(githubIcon);
        infoPageManager.setHelpIcon(helpIcon);
        infoPageManager.init();
    }

    private void initListener() {
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adLoaded = true;
                moveSpeedDial(true);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adLoaded = false;
                moveSpeedDial(false);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adLoaded = false;
                moveSpeedDial(false);
            }
        });

        revealButton.setOnClickListener((View v) -> {
                infoPageManager.start(false);
        });

        reverseRevealButton.setOnClickListener((View v) -> {
                infoPageManager.start(true);
        });

        speedDialView.setOnActionSelectedListener((SpeedDialActionItem actionItem) -> {
                if (actionItem.getId() == uploadID) {
                    storageManager.upload();
                } else if (actionItem.getId() == downloadID) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    LinearLayoutCompat linearLayoutCompat = new LinearLayoutCompat(activity);
                    inflater.inflate(R.layout.download_dialog, linearLayoutCompat);
                    AppCompatEditText appCompatEditText = linearLayoutCompat.findViewById(R.id.download_edittext);
                    activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                            .setTitle("Download file")
                            .setView(linearLayoutCompat)
                            .setPositiveButton(activity.getString(R.string.okay), (dialog, which) -> {
                                storageManager.download(String.valueOf(appCompatEditText.getText()));
                            })
                            .setNegativeButton(activity.getString(R.string.cancel), null)
                            .create()).show();
                }
                return false;
        });

        tuneButton.setOnClickListener((View v) -> {
                settingsManager.open();
        });

    }

    private void initDeeplink() {
        Uri data = activity.getIntent().getData();
        if (data != null && data.isHierarchical()) {
            String url = activity.getIntent().getDataString();
            if (isURLValid(url)) {
                askDownload(url);
            }
        }
    }

    private void initReceive() {
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND) && type != null) {
            if (type.equals("text/plain")) {
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (isURLValid(url)) {
                    Log.e("Download", url);
                    askDownload(url);
                }
            } else {
                //ToDo: upload or search device
            }
        }
    }

    private void askDownload(String url) {
        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                .setTitle("Cloud File")
                .setMessage("Do you want to download the file from this link:\n\n"+url)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    storageManager.download(url);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                })
                .setNeutralButton("Copy Link", (dialog, which) -> {
                    activity.copyText("HotDrop Download URL", url);
                })
                .create()).show();
    }

    @Contract(pure = true)
    private boolean isURLValid(@NotNull String url) {
        return url.contains("gs://hotdrop-420.appspot.com");
    }

    private void moveSpeedDial(boolean up) {
        if (up) {
            if (!speedDialMoved && adLoaded) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) speedDialView.getLayoutParams();
                params.setMargins(params.getMarginStart(), params.topMargin, params.getMarginEnd(), params.bottomMargin + getActionBarHeight());
                speedDialView.setLayoutParams(params);
                speedDialMoved = true;
            }
        } else {
            if (speedDialMoved) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) speedDialView.getLayoutParams();
                params.setMargins(params.getMarginStart(), params.topMargin, params.getMarginEnd(), params.bottomMargin - getActionBarHeight());
                speedDialView.setLayoutParams(params);
                speedDialMoved = false;
            }
        }
    }

    @Override
    public void onChooseFragmentInteraction(Host host) {
        discoverHost.send(host, DiscoverHost.MESSAGE_REQUEST_START_TRANSFER.getBytes());
    }

    @Override
    protected void onOrientationLandscape() {
        moveSpeedDial(false);
    }

    @Override
    protected void onOrientationPortrait() {
        moveSpeedDial(true);
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
        moveSpeedDial(false);
        discoverHost.stopDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        moveSpeedDial(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity.finishAffinity();
        discoverHost.stopDiscovery();
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
