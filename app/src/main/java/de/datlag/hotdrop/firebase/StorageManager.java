package de.datlag.hotdrop.firebase;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.manager.SettingsManager;
import de.datlag.hotdrop.view.helper.MaterialSnackbar;
import de.interaapps.firebasemanager.core.FirebaseManager;
import de.interaapps.firebasemanager.core.auth.Auth;

import static android.content.Context.CLIPBOARD_SERVICE;

public class StorageManager {
    private Activity activity;
    private FirebaseManager firebaseManager;
    private SettingsManager settingsManager;
    private FirebaseUser firebaseUser;
    private UploadManager uploadManager;

    public StorageManager(Activity activity, FirebaseManager firebaseManager, SettingsManager settingsManager) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;
        this.settingsManager = settingsManager;

        init();
    }

    private void init() {
        firebaseUser = getUser();
        uploadManager = new UploadManager(activity, firebaseUser);
    }

    private boolean checkLoginValid() {
        if (firebaseUser == null) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.account))
                    .setMessage(activity.getString(R.string.upload_info))
                    .setPositiveButton(activity.getString(R.string.okay), (DialogInterface dialogInterface, int i) -> {
                        settingsManager.chooseSetting(0);
                    }).setNegativeButton(activity.getString(R.string.cancel), (DialogInterface dialogInterface, int i) -> {
                // ToDo: INSERT
            }).create().show();
            return false;
        }

        return true;
    }

    public void upload() {
        if(checkLoginValid()) {
            uploadManager.startUploadFile(firebaseUser.isAnonymous(), new UploadManager.FileUploadCallback() {
                @Override
                public void onSuccess(String downloadUri) {
                    if (firebaseUser.isAnonymous()) {
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle("File uploaded")
                                .setMessage("Please share this link, otherwise you cannot Download this file or have any access to it")
                                .setPositiveButton("Share", (dialogInterface, i) -> {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Get this file from HotDrop!");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, downloadUri);
                                    sendIntent.setType("text/plain");
                                    activity.startActivity(Intent.createChooser(sendIntent, "Share URL"));
                                }).setNeutralButton("Copy Link", (dialogInterface, i) -> {
                            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("DownloadUrl", downloadUri);
                            Objects.requireNonNull(clipboard).setPrimaryClip(clip);
                        })
                                .create().show();
                    } else {
                        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "File uploaded!", Snackbar.LENGTH_LONG);
                        MaterialSnackbar.configSnackbar(activity, snackbar);
                        snackbar.show();
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Upload failed!", Snackbar.LENGTH_LONG);
                    MaterialSnackbar.configSnackbar(activity, snackbar);
                    snackbar.show();
                }
            });
        }
    }

    private FirebaseUser getUser() {
        for (Auth auth : firebaseManager.getLogin()) {
            if (auth.getUser() != null) {
                return auth.getUser();
            }
        }

        return FirebaseAuth.getInstance().getCurrentUser();
    }
}