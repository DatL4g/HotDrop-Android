package de.datlag.hotdrop.firebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.manager.SettingsManager;
import de.datlag.hotdrop.view.helper.MaterialSnackbar;
import de.interaapps.firebasemanager.core.FirebaseManager;
import de.interaapps.firebasemanager.core.auth.Auth;

public class StorageManager {
    private AdvancedActivity activity;
    private FirebaseManager firebaseManager;
    private SettingsManager settingsManager;
    private FirebaseUser firebaseUser;
    private UploadManager uploadManager;
    private DownloadManager downloadManager;

    public StorageManager(AdvancedActivity activity, FirebaseManager firebaseManager, SettingsManager settingsManager) {
        this.activity = activity;
        this.firebaseManager = firebaseManager;
        this.settingsManager = settingsManager;

        init();
    }

    private void init() {
        firebaseUser = getUser();
        uploadManager = new UploadManager(activity, firebaseUser);
        downloadManager = new DownloadManager(activity, firebaseUser);
    }

    private boolean checkLoginValid() {
        if (firebaseUser == null) {
            activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.account))
                    .setMessage(activity.getString(R.string.upload_info))
                    .setPositiveButton(activity.getString(R.string.okay), (DialogInterface dialogInterface, int i) -> settingsManager.chooseSetting(0))
                    .setNegativeButton(activity.getString(R.string.cancel), (DialogInterface dialogInterface, int i) -> dialogInterface.cancel())
                    .create()).show();
            return false;
        }

        return true;
    }

    public void upload() {
        if (checkLoginValid()) {
            uploadManager.startUploadFile(firebaseUser.isAnonymous(), new UploadManager.FileUploadCallback() {
                @Override
                public void onSuccess(String downloadUri) {
                    if (firebaseUser.isAnonymous()) {
                        activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                                .setTitle("File uploaded")
                                .setMessage("Please share this link, otherwise you cannot Download this file or have any access to it")
                                .setPositiveButton("Share", (dialogInterface, i) -> {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Get this file from HotDrop!");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, downloadUri);
                                    sendIntent.setType("text/plain");
                                    activity.startActivity(Intent.createChooser(sendIntent, "Share URL"));
                                }).setNeutralButton("Copy Link", (dialogInterface, i) -> {
                                    activity.copyText("HotDrop Download URL", downloadUri);
                                })
                                .create()).show();
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
                    Log.e("Upload", exception.getMessage());
                }
            });
        }
    }

    public void download(String url) {
        if (checkLoginValid()) {
            if (firebaseUser.isAnonymous()) {
                downloadManager.startDownload(url, new DownloadManager.FileDownloadCallback() {
                    @Override
                    public void onNotFound() {
                        Log.e("File", "Not found");
                    }

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailed(Exception exception) {

                    }
                });
            } else {

            }
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